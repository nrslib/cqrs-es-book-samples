// 紙面照合: 3.21
package org.example.cqrsesbooksamples.chapter03._16;

import org.axonframework.eventhandling.*;
import org.axonframework.common.DateTimeUtils;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.ConcatenatingDomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 階層型イベントストレージエンジン
 * アクティブストレージとアーカイブストレージの2層構造で、古いイベントを自動的にアーカイブする
 *
 * <p>イベントの追加は、このインスタンスを経由してactive側へ行う必要がある。archiveエンジンへ
 * 直接appendすると、globalIndexの明示INSERTとarchive側の自動採番が衝突するため禁止する。</p>
 *
 * <p>有限の追跡読み取りはactive側とarchive側をglobalIndexでマージし、両ストアを再走査することで、
 * 読み取り中に移動したイベントも取得する。{@code mayBlock=true}でactive側のブロッキング読み取りへ
 * 移行した後はarchiveへ戻らないため、アーカイブ閾値はイベントプロセッサの最大遅延より十分長く
 * 設定する必要がある。同一globalIndexの識別子照合は有限モードのマージで行い、blockingモードは
 * アーカイバが維持する不変条件に依存する。アーカイブ処理とリビルドの厳密な同時実行でも順序保証が
 * 必要な場合は、アーカイブ処理を停止してからリビルドする。</p>
 */

public class TieredEventStorageEngine implements EventStorageEngine {
    private static final Logger logger = LoggerFactory.getLogger(TieredEventStorageEngine.class);
    // JdbcEventStorageEngineの既定値と揃え、統合後も同じgap保持範囲にする。
    private static final int MAX_GAP_OFFSET = 10_000;

    private final EventStorageEngine activeStorage;
    private final EventStorageEngine archiveStorage;
    private final Duration archiveThreshold;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate activeJdbcTemplate;
    private final JdbcTemplate archiveJdbcTemplate;
    private final int batchSize;

    /**
     * コンストラクタ
     *
     * @param activeStorage アクティブストレージ (最新イベント保存用)
     * @param archiveStorage アーカイブストレージ (古いイベント保存用)
     * @param archiveThreshold アーカイブ閾値 (この期間より古いイベントがアーカイブ対象)
     * @param transactionManager トランザクションマネージャ
     * @param activeDataSource アクティブ用データソース
     * @param archiveDataSource アーカイブ用データソース
     * @param batchSize バッチサイズ (アーカイブ処理時の1バッチあたりの処理件数)
     */
    public TieredEventStorageEngine(EventStorageEngine activeStorage,
                                    EventStorageEngine archiveStorage,
                                    Duration archiveThreshold,
                                    PlatformTransactionManager transactionManager,
                                    DataSource activeDataSource,
                                    DataSource archiveDataSource,
                                    int batchSize) {
        this.activeStorage = activeStorage;
        this.archiveStorage = archiveStorage;
        this.archiveThreshold = archiveThreshold;
        this.transactionManager = transactionManager;
        this.activeJdbcTemplate = new JdbcTemplate(activeDataSource);
        this.archiveJdbcTemplate = new JdbcTemplate(archiveDataSource);
        this.batchSize = batchSize;
    }

    /**
     * シンプルコンストラクタ (トランザクションマネージャやバッチサイズのデフォルト値使用)
     *
     * @param activeStorage アクティブストレージ
     * @param archiveStorage アーカイブストレージ
     * @param archiveThreshold アーカイブ閾値
     * @param activeDataSource アクティブデータソース
     * @param archiveDataSource アーカイブデータソース
     */
    public TieredEventStorageEngine(EventStorageEngine activeStorage,
                                    EventStorageEngine archiveStorage,
                                    Duration archiveThreshold,
                                    DataSource activeDataSource,
                                    DataSource archiveDataSource) {
        this(activeStorage, archiveStorage, archiveThreshold, null, activeDataSource, archiveDataSource, 100);
    }

    /**
     * アクティブストレージを取得
     */
    public EventStorageEngine getActiveStorage() {
        return activeStorage;
    }

    /**
     * アーカイブストレージを取得
     */
    public EventStorageEngine getArchiveStorage() {
        return archiveStorage;
    }

    @Override
    public void appendEvents(List<? extends EventMessage<?>> events) {
        // 新しいイベントはアクティブストレージにのみ追加
        activeStorage.appendEvents(events);
    }

    @Override
    public void storeSnapshot(DomainEventMessage<?> snapshot) {
        // スナップショットはアクティブストレージにのみ保存
        activeStorage.storeSnapshot(snapshot);
    }

    @Override
    public DomainEventStream readEvents(String aggregateIdentifier) {
        // アーカイブとアクティブの両ストレージのイベントを古い順につなげて返す
        return new ConcatenatingDomainEventStream(
                archiveStorage.readEvents(aggregateIdentifier),
                activeStorage.readEvents(aggregateIdentifier)
        );
    }

    @Override
    public Optional<DomainEventMessage<?>> readSnapshot(String aggregateIdentifier) {
        // アクティブストレージから取得を試み、なければアーカイブストレージから取得
        var snapshot = activeStorage.readSnapshot(aggregateIdentifier);
        if (!snapshot.isPresent()) {
            snapshot = archiveStorage.readSnapshot(aggregateIdentifier);
        }

        return snapshot;
    }

    @Override
    public Stream<? extends TrackedEventMessage<?>> readEvents(TrackingToken trackingToken, boolean mayBlock) {
        class TieredTrackingSpliterator
                extends Spliterators.AbstractSpliterator<TrackedEventMessage<?>> {
            private GapAwareTrackingToken currentToken = asGapAwareToken(trackingToken);
            private final SourceCursor archiveCursor = new SourceCursor(archiveStorage, false);
            private final SourceCursor activeCursor = new SourceCursor(activeStorage, mayBlock);
            private boolean readingBlockingActive;
            private boolean finished;
            private boolean gapRechecked;
            private long gapRecheckedFromIndex;
            private long gapRecheckedToIndex;

            private TieredTrackingSpliterator() {
                super(Long.MAX_VALUE, Spliterator.ORDERED);
            }

            @Override
            public boolean tryAdvance(Consumer<? super TrackedEventMessage<?>> action) {
                try {
                    if (mayBlock) {
                        return tryAdvanceBlocking(action);
                    }
                    return tryAdvanceFinite(action);
                } catch (RuntimeException e) {
                    close();
                    throw e;
                }
            }

            private boolean tryAdvanceBlocking(Consumer<? super TrackedEventMessage<?>> action) {
                if (!readingBlockingActive && archiveCursor.fill()) {
                    emit(archiveCursor.take(), action);
                    return true;
                }
                readingBlockingActive = true;
                if (activeCursor.fill()) {
                    emit(activeCursor.take(), action);
                    return true;
                }
                close();
                return false;
            }

            private boolean tryAdvanceFinite(Consumer<? super TrackedEventMessage<?>> action) {
                while (!finished) {
                    boolean archiveHasEvent = archiveCursor.fill();
                    boolean activeHasEvent = activeCursor.fill();

                    if (archiveCursor.exhaustedAfterEvents()) {
                        activeCursor.requireRecheck();
                    }
                    if (activeCursor.exhaustedAfterEvents()) {
                        archiveCursor.requireRecheck();
                    }
                    if (!archiveHasEvent && archiveCursor.shouldRetryBeforeMerge()) {
                        continue;
                    }
                    if (!activeHasEvent && activeCursor.shouldRetryBeforeMerge()) {
                        continue;
                    }
                    if (!archiveHasEvent && !activeHasEvent) {
                        close();
                        return false;
                    }

                    IndexedEvent archiveEvent = archiveCursor.peek();
                    IndexedEvent activeEvent = activeCursor.peek();
                    IndexedEvent nextEvent;
                    boolean duplicate = false;
                    if (archiveEvent == null) {
                        nextEvent = activeEvent;
                    } else if (activeEvent == null) {
                        nextEvent = archiveEvent;
                    } else if (archiveEvent.globalIndex < activeEvent.globalIndex) {
                        nextEvent = archiveEvent;
                    } else if (activeEvent.globalIndex < archiveEvent.globalIndex) {
                        nextEvent = activeEvent;
                    } else {
                        validateDuplicate(archiveEvent, activeEvent);
                        nextEvent = archiveEvent;
                        duplicate = true;
                    }

                    if (recheckGapBefore(nextEvent)) {
                        continue;
                    }
                    if (duplicate) {
                        archiveCursor.take();
                        activeCursor.take();
                    } else if (nextEvent == archiveEvent) {
                        archiveCursor.take();
                    } else {
                        activeCursor.take();
                    }
                    emit(nextEvent, action);
                    return true;
                }
                return false;
            }

            private void validateDuplicate(IndexedEvent archiveEvent,
                                           IndexedEvent activeEvent) {
                if (!archiveEvent.event.getIdentifier().equals(activeEvent.event.getIdentifier())) {
                    throw new IllegalStateException(
                            "Different events share globalIndex " + archiveEvent.globalIndex
                    );
                }
            }

            private boolean recheckGapBefore(IndexedEvent indexedEvent) {
                if (currentToken == null) {
                    return false;
                }
                long currentIndex = currentToken.getIndex();
                if (indexedEvent.globalIndex <= currentIndex
                        || indexedEvent.globalIndex == currentIndex + 1) {
                    return false;
                }
                if (gapRechecked
                        && gapRecheckedFromIndex == currentIndex
                        && gapRecheckedToIndex == indexedEvent.globalIndex) {
                    return false;
                }

                gapRechecked = true;
                gapRecheckedFromIndex = currentIndex;
                gapRecheckedToIndex = indexedEvent.globalIndex;
                archiveCursor.requireRecheck();
                activeCursor.requireRecheck();
                return true;
            }

            private void emit(IndexedEvent indexedEvent,
                              Consumer<? super TrackedEventMessage<?>> action) {
                currentToken = currentToken == null
                        ? indexedEvent.sourceToken
                        : currentToken.advanceTo(indexedEvent.globalIndex, MAX_GAP_OFFSET);
                gapRechecked = false;
                action.accept(indexedEvent.event.withTrackingToken(currentToken));
            }

            private void close() {
                finished = true;
                try {
                    archiveCursor.close();
                } finally {
                    activeCursor.close();
                }
            }

            private final class SourceCursor {
                private final EventStorageEngine storage;
                private final boolean blocks;
                private Stream<? extends TrackedEventMessage<?>> stream;
                private Spliterator<? extends TrackedEventMessage<?>> spliterator;
                private GapAwareTrackingToken sourceToken;
                private IndexedEvent nextEvent;
                private int emptyReads;
                private boolean currentReadHasEvents;
                private boolean exhaustedAfterEvents;

                private SourceCursor(EventStorageEngine storage, boolean blocks) {
                    this.storage = storage;
                    this.blocks = blocks;
                }

                private boolean fill() {
                    exhaustedAfterEvents = false;
                    if (nextEvent != null) {
                        return true;
                    }
                    if (spliterator == null) {
                        if (emptyReads >= 2) {
                            return false;
                        }
                        stream = storage.readEvents(currentToken, blocks);
                        spliterator = stream.spliterator();
                        sourceToken = currentToken;
                        currentReadHasEvents = false;
                    }

                    AtomicReference<TrackedEventMessage<?>> eventReference = new AtomicReference<>();
                    if (spliterator.tryAdvance(eventReference::set)) {
                        TrackedEventMessage<?> event = eventReference.get();
                        GapAwareTrackingToken eventToken = asGapAwareToken(event.trackingToken());
                        long globalIndex = eventIndex(sourceToken, eventToken);
                        sourceToken = eventToken;
                        nextEvent = new IndexedEvent(event, eventToken, globalIndex);
                        currentReadHasEvents = true;
                        emptyReads = 0;
                        return true;
                    }

                    boolean exhaustedNonEmptyRead = currentReadHasEvents;
                    closeStream();
                    emptyReads++;
                    exhaustedAfterEvents = exhaustedNonEmptyRead;
                    return false;
                }

                private boolean shouldRetryBeforeMerge() {
                    return emptyReads == 1;
                }

                private boolean exhaustedAfterEvents() {
                    return exhaustedAfterEvents;
                }

                private IndexedEvent peek() {
                    return nextEvent;
                }

                private IndexedEvent take() {
                    IndexedEvent result = nextEvent;
                    nextEvent = null;
                    return result;
                }

                private void requireRecheck() {
                    if (emptyReads >= 2) {
                        emptyReads = 1;
                    }
                }

                private void closeStream() {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                    } finally {
                        stream = null;
                        spliterator = null;
                        sourceToken = null;
                        currentReadHasEvents = false;
                    }
                }

                private void close() {
                    nextEvent = null;
                    closeStream();
                }
            }

            private final class IndexedEvent {
                private final TrackedEventMessage<?> event;
                private final GapAwareTrackingToken sourceToken;
                private final long globalIndex;

                private IndexedEvent(TrackedEventMessage<?> event,
                                     GapAwareTrackingToken sourceToken,
                                     long globalIndex) {
                    this.event = event;
                    this.sourceToken = sourceToken;
                    this.globalIndex = globalIndex;
                }
            }
        }

        TieredTrackingSpliterator spliterator = new TieredTrackingSpliterator();
        return StreamSupport.stream(spliterator, false).onClose(spliterator::close);
    }

    private GapAwareTrackingToken asGapAwareToken(TrackingToken token) {
        if (token == null) {
            return null;
        }
        if (!(token instanceof GapAwareTrackingToken gapAwareToken)) {
            throw new IllegalArgumentException(
                    "Tiered storage requires GapAwareTrackingToken, got " + token.getClass().getSimpleName()
            );
        }
        return gapAwareToken;
    }

    private long eventIndex(GapAwareTrackingToken previousToken,
                            GapAwareTrackingToken eventToken) {
        if (previousToken == null || eventToken.getIndex() > previousToken.getIndex()) {
            return eventToken.getIndex();
        }
        if (eventToken.getIndex() < previousToken.getIndex()) {
            throw new IllegalStateException("Tracking token position moved backwards");
        }

        SortedSet<Long> removedGaps = new TreeSet<>(previousToken.getGaps());
        removedGaps.removeAll(eventToken.getGaps());
        if (removedGaps.isEmpty()) {
            throw new IllegalStateException("Tracking token did not advance");
        }
        // gap切り捨てが同時に起きても、実際に消費したイベントは除去されたgapの最大indexになる。
        return removedGaps.last();
    }

    @Override
    public TrackingToken createTailToken() {
        TrackingToken archiveToken = archiveStorage.createTailToken();
        return archiveToken != null ? archiveToken : activeStorage.createTailToken();
    }

    @Override
    public TrackingToken createHeadToken() {
        TrackingToken activeToken = activeStorage.createHeadToken();
        return activeToken != null ? activeToken : archiveStorage.createHeadToken();
    }

    @Override
    public TrackingToken createTokenAt(Instant instant) {
        TrackingToken activeToken = activeStorage.createTokenAt(instant);
        if (activeToken == null) {
            return archiveStorage.createTokenAt(instant);
        }

        TrackingToken activeTailToken = activeStorage.createTailToken();
        if (!activeToken.equals(activeTailToken)) {
            return activeToken;
        }

        TrackingToken archiveToken = archiveStorage.createTokenAt(instant);
        if (archiveToken == null || archiveToken.equals(archiveStorage.createHeadToken())) {
            return activeToken;
        }
        return archiveToken;
    }

    @Override
    public DomainEventStream readEvents(String aggregateIdentifier, long firstSequenceNumber) {
        return new ConcatenatingDomainEventStream(
                archiveStorage.readEvents(aggregateIdentifier, firstSequenceNumber),
                activeStorage.readEvents(aggregateIdentifier, firstSequenceNumber)
        );
    }

    /**
     * アーカイブ処理を実行
     * 指定された閾値より古いイベントをアクティブストレージからアーカイブストレージに移動
     */
    public void archiveEvents() {
        logger.info("Starting event archiving process...");
        Instant cutoffPoint = Instant.now().minus(archiveThreshold);

        try {
            // JDBCストレージを使用している場合の特別な処理
            if (activeStorage instanceof JdbcEventStorageEngine && archiveStorage instanceof JdbcEventStorageEngine) {
                archiveEventsWithJdbc(cutoffPoint);
            } else {
                // 汎用的なアーカイブ処理
                archiveEventsGeneric(cutoffPoint);
            }
            logger.info("Event archiving completed successfully");
        } catch (Exception e) {
            logger.error("Error during event archiving", e);
            throw new RuntimeException("Failed to archive events", e);
        }
    }

    /**
     * JDBCストレージエンジン向けの最適化されたアーカイブ処理
     */
    private void archiveEventsWithJdbc(Instant cutoffPoint) {
        logger.info("Using JDBC optimized archiving");

        // タイムスタンプ文字列（Axon形式）を取得
        String cutoffTimestamp = DateTimeUtils.formatInstant(cutoffPoint);

        logger.debug("Archiving events older than: {}", cutoffTimestamp);

        // 境界を1クエリで固定し、算出後のappendを今回の移動対象から除外する
        Long archiveBoundary = activeJdbcTemplate.queryForObject(
                "SELECT COALESCE(" +
                        "MIN(CASE WHEN timeStamp >= ? THEN globalIndex END), " +
                        "MAX(globalIndex) + 1) " +
                        "FROM DomainEventEntry",
                Long.class,
                cutoffTimestamp
        );
        if (archiveBoundary == null) {
            logger.debug("No events to archive");
            return;
        }

        // トランザクション処理
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        int totalArchived = 0;
        boolean hasMore = true;

        while (hasMore) {
            TransactionStatus status = null;

            try {
                if (transactionManager != null) {
                    status = transactionManager.getTransaction(def);
                }

                // 1. アクティブストレージから古いイベントを取得
                List<Object[]> oldEvents = activeJdbcTemplate.query(
                        "SELECT globalIndex, aggregateIdentifier, sequenceNumber, type, eventIdentifier, " +
                                "metaData, payload, payloadRevision, payloadType, timeStamp " +
                                "FROM DomainEventEntry WHERE globalIndex < ?" +
                                " ORDER BY globalIndex LIMIT ?",
                        (rs, rowNum) -> {
                            String timestamp = rs.getString("timeStamp");
                            String eventId = rs.getString("eventIdentifier");
                            String aggregateId = rs.getString("aggregateIdentifier");
                            Long sequenceNumber = rs.getLong("sequenceNumber");

                            logger.debug("Processing event: {} (aggregateId={}, seq={}) with timestamp: {}",
                                    eventId, aggregateId, sequenceNumber, timestamp);

                            return new Object[] {
                                    rs.getLong("globalIndex"),
                                    aggregateId,
                                    sequenceNumber,
                                    rs.getString("type"),
                                    eventId,
                                    rs.getBytes("metaData"),
                                    rs.getBytes("payload"),
                                    rs.getString("payloadRevision"),
                                    rs.getString("payloadType"),
                                    timestamp
                            };
                        },
                        archiveBoundary, batchSize
                );

                if (oldEvents.isEmpty()) {
                    hasMore = false;
                    logger.debug("No more events to archive");
                } else {
                    // 2. アーカイブストレージに挿入
                    for (Object[] event : oldEvents) {
                        String aggregateId = (String) event[1];
                        Long sequenceNumber = (Long) event[2];
                        String eventId = (String) event[4];

                        List<String> archivedEventIds = archiveJdbcTemplate.queryForList(
                                "SELECT eventIdentifier FROM DomainEventEntry WHERE globalIndex = ?",
                                String.class,
                                event[0]
                        );
                        if (archivedEventIds.isEmpty()) {
                            archiveJdbcTemplate.update(
                                    "INSERT INTO DomainEventEntry " +
                                            "(globalIndex, aggregateIdentifier, sequenceNumber, type, eventIdentifier, " +
                                            "metaData, payload, payloadRevision, payloadType, timeStamp) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                    event
                            );
                        } else if (!archivedEventIds.get(0).equals(eventId)) {
                            throw new IllegalStateException(
                                    "Archive globalIndex " + event[0] + " belongs to another event"
                            );
                        }
                        logger.debug("Archived event: {} (aggregateId={}, seq={})",
                                eventId, aggregateId, sequenceNumber);
                    }

                    // 3. アクティブストレージから削除
                    if (!oldEvents.isEmpty()) {
                        // イベントIDのリストを作成
                        List<String> eventIds = new ArrayList<>();
                        for (Object[] event : oldEvents) {
                            eventIds.add((String) event[4]); // eventIdentifier
                        }

                        // IN句を構築して一括削除
                        StringBuilder inClause = new StringBuilder();
                        for (int i = 0; i < eventIds.size(); i++) {
                            if (i > 0) inClause.append(",");
                            inClause.append("?");
                        }

                        int deletedCount = activeJdbcTemplate.update(
                                "DELETE FROM DomainEventEntry WHERE eventIdentifier IN (" + inClause.toString() + ")",
                                eventIds.toArray()
                        );
                        logger.debug("Deleted {} events from active storage", deletedCount);
                    }

                    totalArchived += oldEvents.size();

                    logger.info("Archived batch of {} events, total: {}", oldEvents.size(), totalArchived);
                }

                if (transactionManager != null && status != null) {
                    transactionManager.commit(status);
                }
            } catch (Exception e) {
                if (transactionManager != null && status != null) {
                    transactionManager.rollback(status);
                }
                logger.error("Error during JDBC archiving", e);
                throw new RuntimeException("Failed to archive events with JDBC", e);
            }
        }

        logger.info("JDBC archiving completed: {} events archived", totalArchived);
    }

    /**
     * 汎用的なストレージエンジン向けのアーカイブ処理
     */
    private void archiveEventsGeneric(Instant cutoffPoint) {
        logger.info("Using generic archiving strategy");
        // 実装略（APIを使ってイベントをページングで取得し、アーカイブする処理）
        // この部分は従来のコードからほとんど変更なし
    }

}
