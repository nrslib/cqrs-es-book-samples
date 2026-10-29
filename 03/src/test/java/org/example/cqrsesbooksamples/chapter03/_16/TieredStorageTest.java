// 検証補助: 3.21
package org.example.cqrsesbooksamples.chapter03._16;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.GenericTrackedEventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.jdbc.SpringDataSourceConnectionProvider;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 階層型ストレージのテストクラス
 * Spring Bootの自動設定に依存せず、テスト内で直接データソースを作成します
 */
public class TieredStorageTest {

    private TieredEventStorageEngine tieredStorage;
    private DataSource activeDataSource;
    private DataSource archiveDataSource;
    private Serializer eventSerializer;
    private Serializer snapshotSerializer;
    private String testAggregateId;
    private PlatformTransactionManager activeSpringTransactionManager;
    private PlatformTransactionManager archiveSpringTransactionManager;
    private TransactionManager activeTransactionManager;
    private TransactionManager archiveTransactionManager;
    private JdbcTemplate activeJdbcTemplate;
    private JdbcTemplate archiveJdbcTemplate;

    @BeforeEach
    void setUp() {
        // テスト用のデータソースを準備（アクティブストレージ用H2インメモリDB）
        activeDataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("active-db-" + UUID.randomUUID())
                .addScript("schema.sql") // クラスパス上のスキーマ定義
                .build();

        // アーカイブストレージ用のH2インメモリDB
        archiveDataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("archive-db-" + UUID.randomUUID())
                .addScript("schema.sql")
                .build();

        // JdbcTemplateを初期化
        activeJdbcTemplate = new JdbcTemplate(activeDataSource);
        archiveJdbcTemplate = new JdbcTemplate(archiveDataSource);

        // Springトランザクションマネージャの設定
        activeSpringTransactionManager = new DataSourceTransactionManager(activeDataSource);
        archiveSpringTransactionManager = new DataSourceTransactionManager(archiveDataSource);

        // Axonトランザクションマネージャの設定（Springのトランザクションマネージャをラップ）
        activeTransactionManager = new SpringTransactionManager(activeSpringTransactionManager);
        archiveTransactionManager = new SpringTransactionManager(archiveSpringTransactionManager);

        // シリアライザの設定
        eventSerializer = JacksonSerializer.builder().build();
        snapshotSerializer = JacksonSerializer.builder().build();

        // イベントスキーマの設定
        EventSchema eventSchema = EventSchema.builder()
                .eventTable("DomainEventEntry")
                .snapshotTable("SnapshotEventEntry")
                .build();

        // アクティブストレージ
        JdbcEventStorageEngine activeStorage = JdbcEventStorageEngine.builder()
                .connectionProvider(new SpringDataSourceConnectionProvider(activeDataSource))
                .transactionManager(activeTransactionManager)
                .eventSerializer(eventSerializer)
                .snapshotSerializer(snapshotSerializer)
                .schema(eventSchema)
                .batchSize(4)
                .build();

        // アーカイブストレージ
        JdbcEventStorageEngine archiveStorage = JdbcEventStorageEngine.builder()
                .connectionProvider(new SpringDataSourceConnectionProvider(archiveDataSource))
                .transactionManager(archiveTransactionManager)
                .eventSerializer(eventSerializer)
                .snapshotSerializer(snapshotSerializer)
                .schema(eventSchema)
                .batchSize(4)
                .build();

        // 階層型ストレージの設定（アーカイブ閾値を短く設定）
        tieredStorage = new TieredEventStorageEngine(
                activeStorage,
                archiveStorage,
                Duration.ofDays(1),
                activeSpringTransactionManager,
                activeDataSource,
                archiveDataSource,
                2);

        // テスト用のアグリゲートID
        testAggregateId = UUID.randomUUID().toString();

    }


    @Test
    void archivingComparesTimesWithinTheCutoffDateIncludingMilliseconds() {
        var cutoff = Instant.parse("2026-09-13T12:00:00.123Z");
        var now = cutoff.plus(Duration.ofDays(1));
        tieredStorage.appendEvents(createEvents(testAggregateId, 0, 1, "Before", cutoff.minusMillis(1)));
        tieredStorage.appendEvents(createEvents(testAggregateId, 1, 1, "Equal", cutoff));
        tieredStorage.appendEvents(createEvents(testAggregateId, 2, 1, "After", cutoff.plusMillis(1)));
        assertEquals(List.of("2026-09-13T12:00:00.122Z", "2026-09-13T12:00:00.123Z", "2026-09-13T12:00:00.124Z"),
            activeJdbcTemplate.queryForList("SELECT timeStamp FROM DomainEventEntry ORDER BY globalIndex", String.class));

        try (var clock = org.mockito.Mockito.mockStatic(Instant.class, org.mockito.Mockito.CALLS_REAL_METHODS)) {
            clock.when(Instant::now).thenReturn(now);
            tieredStorage.archiveEvents();
        }

        assertEquals(1, countEventsInArchiveStorage(testAggregateId));
        assertEquals(2, countEventsInActiveStorage(testAggregateId));
        assertEquals(List.of("Before0"), collectEvents(tieredStorage.getArchiveStorage().readEvents(testAggregateId))
            .stream().map(DomainEventMessage::getPayload).toList());
        assertEquals(List.of("Equal1", "After2"), collectEvents(tieredStorage.getActiveStorage().readEvents(testAggregateId))
            .stream().map(DomainEventMessage::getPayload).toList());
    }

    @Test
    void testEventStorageAndArchiving() {
        // 1. まず古いイベントを作成（現在時刻より前の時間帯のイベント）
        List<DomainEventMessage<?>> oldEvents = createOldTestEvents(testAggregateId, 0, 3);
        tieredStorage.appendEvents(oldEvents);

        // 2. 次に新しいイベントをアクティブストレージに追加
        List<DomainEventMessage<?>> newEvents = createTestEvents(testAggregateId, 3, 5);
        tieredStorage.appendEvents(newEvents);

        // アーカイブ前の合計イベント数を確認
        List<DomainEventMessage<?>> eventList = collectEvents(tieredStorage.readEvents(testAggregateId));
        int totalCountBefore = eventList.size();

        assertEquals(8, totalCountBefore, "アーカイブ前の合計イベント数");

        // 3. アーカイブ処理を実行
        tieredStorage.archiveEvents();

        // 4. アーカイブ後も全イベントが読み取れることを確認
        List<DomainEventMessage<?>> afterEventList = collectEvents(tieredStorage.readEvents(testAggregateId));
        int totalCountAfter = afterEventList.size();

        assertEquals(8, totalCountAfter, "アーカイブ後の合計イベント数が変わっていないはず");

        // 5. アクティブストレージとアーカイブストレージの両方から直接クエリでイベント数を確認
        int activeCount = countEventsInActiveStorage(testAggregateId);
        int archiveCount = countEventsInArchiveStorage(testAggregateId);

        // 古いイベントがアーカイブされていることを確認
        assertEquals(5, activeCount, "新しいイベントのみがアクティブストレージに残っているはず");
        assertEquals(3, archiveCount, "古いイベントのみがアーカイブストレージに移動しているはず");
        assertEquals(8, activeCount + archiveCount, "合計は変わらないはず");
    }

    @Test
    void trackingStreamReplaysArchivedAndActiveEventsExactlyOnceInGlobalIndexOrder() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 3));
        tieredStorage.appendEvents(createTestEvents(testAggregateId, 3, 2));
        tieredStorage.archiveEvents();

        TrackingToken tailToken = tieredStorage.createTailToken();
        assertNotNull(tailToken);
        List<? extends TrackedEventMessage<?>> events;
        try (var stream = tieredStorage.readEvents(tailToken, false)) {
            events = stream.collect(Collectors.toList());
        }

        assertEquals(
                List.of("OldEvent0", "OldEvent1", "OldEvent2", "Event3", "Event4"),
                events.stream().map(TrackedEventMessage::getPayload).collect(Collectors.toList())
        );
        List<Long> positions = events.stream()
                .map(event -> event.trackingToken().position().orElseThrow())
                .collect(Collectors.toList());
        List<Long> storedGlobalIndices = new ArrayList<>();
        storedGlobalIndices.addAll(archiveJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry", Long.class));
        storedGlobalIndices.addAll(activeJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry", Long.class));
        storedGlobalIndices.sort(Long::compareTo);

        assertEquals(storedGlobalIndices, positions);
        assertEquals(positions.size(), positions.stream().distinct().count());
        GapAwareTrackingToken lastToken = (GapAwareTrackingToken) events.get(events.size() - 1).trackingToken();
        assertTrue(lastToken.getGaps().isEmpty());
    }

    @Test
    void aggregateStreamRestoresOriginalPayloadFromArchive() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 2));
        tieredStorage.appendEvents(createTestEvents(testAggregateId, 2, 2));
        tieredStorage.archiveEvents();

        List<Object> payloads = collectEvents(tieredStorage.readEvents(testAggregateId)).stream()
                .map(DomainEventMessage::getPayload)
                .collect(Collectors.toList());
        List<Object> payloadsFromSequenceOne = collectEvents(tieredStorage.readEvents(testAggregateId, 1)).stream()
                .map(DomainEventMessage::getPayload)
                .collect(Collectors.toList());

        assertEquals(List.of("OldEvent0", "OldEvent1", "Event2", "Event3"), payloads);
        assertEquals(List.of("OldEvent1", "Event2", "Event3"), payloadsFromSequenceOne);
    }

    @Test
    void trackingStreamContinuesWithActiveEventsAfterArchiveHead() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 2));
        tieredStorage.appendEvents(createTestEvents(testAggregateId, 2, 2));
        tieredStorage.archiveEvents();

        TrackingToken archiveHeadToken = tieredStorage.getArchiveStorage().createHeadToken();
        assertNotNull(archiveHeadToken);
        List<Object> payloads;
        try (var stream = tieredStorage.readEvents(archiveHeadToken, false)) {
            payloads = stream.map(TrackedEventMessage::getPayload).collect(Collectors.toList());
        }

        assertEquals(List.of("Event2", "Event3"), payloads);
    }

    @Test
    void finiteTrackingStreamRescansArchiveAfterEventsMoveDuringConsumption() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 6));

        List<TrackedEventMessage<?>> events = new ArrayList<>();
        try (var stream = tieredStorage.readEvents(tieredStorage.createTailToken(), false)) {
            Iterator<? extends TrackedEventMessage<?>> iterator = stream.iterator();
            events.add(iterator.next());

            tieredStorage.archiveEvents();

            iterator.forEachRemaining(events::add);
        }

        assertEquals(
                List.of("OldEvent0", "OldEvent1", "OldEvent2", "OldEvent3", "OldEvent4", "OldEvent5"),
                events.stream().map(TrackedEventMessage::getPayload).collect(Collectors.toList())
        );
        List<Long> positions = events.stream()
                .map(event -> event.trackingToken().position().orElseThrow())
                .collect(Collectors.toList());
        List<Long> sortedPositions = new ArrayList<>(positions);
        sortedPositions.sort(Long::compareTo);
        assertEquals(sortedPositions, positions);
        assertEquals(positions.size(), positions.stream().distinct().count());
        assertEquals(0, countEventsInActiveStorage(testAggregateId));
        assertEquals(6, countEventsInArchiveStorage(testAggregateId));
    }

    @Test
    void trackingStreamFillsMovedPrefixBeforeReturningHigherActiveEvents() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 2));
        tieredStorage.appendEvents(createTestEvents(testAggregateId, 2, 2));
        List<Long> globalIndices = activeJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class);

        TieredEventStorageEngine storageWithArchiveReadBarrier = archiveOnFirstArchiveRead();

        List<TrackedEventMessage<?>> events = new ArrayList<>();
        try (var stream = storageWithArchiveReadBarrier.readEvents(
                storageWithArchiveReadBarrier.createTailToken(), false)) {
            stream.forEach(events::add);
        }

        assertEquals(
                List.of("OldEvent0", "OldEvent1", "Event2", "Event3"),
                events.stream().map(TrackedEventMessage::getPayload).collect(Collectors.toList())
        );
        assertEquals(
                globalIndices,
                events.stream()
                        .map(event -> event.trackingToken().position().orElseThrow())
                        .collect(Collectors.toList())
        );
    }

    @Test
    void trackingStreamMergesGapEventsByTheirGlobalIndex() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 1));
        tieredStorage.appendEvents(createTestEvents(testAggregateId, 1, 2));
        List<Long> globalIndices = activeJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class);
        activeJdbcTemplate.update(
                "DELETE FROM DomainEventEntry WHERE globalIndex = ?", globalIndices.get(2));
        GapAwareTrackingToken startToken = GapAwareTrackingToken.newInstance(
                globalIndices.get(2),
                List.of(globalIndices.get(0), globalIndices.get(1))
        );
        TieredEventStorageEngine storageWithArchiveReadBarrier = archiveOnFirstArchiveRead();

        List<TrackedEventMessage<?>> events;
        try (var stream = storageWithArchiveReadBarrier.readEvents(startToken, false)) {
            events = stream.collect(Collectors.toList());
        }

        assertEquals(
                List.of("OldEvent0", "Event1"),
                events.stream().map(TrackedEventMessage::getPayload).collect(Collectors.toList())
        );
        GapAwareTrackingToken finalToken = (GapAwareTrackingToken) events.get(1).trackingToken();
        assertTrue(finalToken.getGaps().isEmpty());
    }

    @Test
    void trackingStreamMergesMovedPrefixWhenStartedWithNullToken() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 2));
        tieredStorage.appendEvents(createTestEvents(testAggregateId, 2, 2));
        List<Long> globalIndices = activeJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class);
        TieredEventStorageEngine storageWithArchiveReadBarrier = archiveOnFirstArchiveRead();

        List<TrackedEventMessage<?>> events;
        try (var stream = storageWithArchiveReadBarrier.readEvents(null, false)) {
            events = stream.collect(Collectors.toList());
        }

        assertEquals(
                List.of("OldEvent0", "OldEvent1", "Event2", "Event3"),
                events.stream().map(TrackedEventMessage::getPayload).collect(Collectors.toList())
        );
        assertEquals(
                globalIndices,
                events.stream()
                        .map(event -> event.trackingToken().position().orElseThrow())
                        .collect(Collectors.toList())
        );
    }

    @Test
    void trackingStreamRetriesWhenAllActiveEventsMoveDuringTheFirstArchiveRead() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 4));
        List<Long> globalIndices = activeJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class);
        TrackingToken tailToken = tieredStorage.createTailToken();
        TieredEventStorageEngine storageWithArchiveReadBarrier = archiveOnFirstArchiveRead();

        List<TrackedEventMessage<?>> events;
        try (var stream = storageWithArchiveReadBarrier.readEvents(tailToken, false)) {
            events = stream.collect(Collectors.toList());
        }

        assertEquals(
                List.of("OldEvent0", "OldEvent1", "OldEvent2", "OldEvent3"),
                events.stream().map(TrackedEventMessage::getPayload).collect(Collectors.toList())
        );
        assertEquals(
                globalIndices,
                events.stream()
                        .map(event -> event.trackingToken().position().orElseThrow())
                        .collect(Collectors.toList())
        );
    }

    @Test
    void trackingStreamDoesNotRequeryEmptyArchiveForEveryActiveEvent() {
        JdbcEventStorageEngine archiveStorage = spy(
                (JdbcEventStorageEngine) tieredStorage.getArchiveStorage()
        );
        TieredEventStorageEngine storageWithCountingArchive = new TieredEventStorageEngine(
                tieredStorage.getActiveStorage(),
                archiveStorage,
                Duration.ofDays(1),
                activeSpringTransactionManager,
                activeDataSource,
                archiveDataSource,
                2
        );
        storageWithCountingArchive.appendEvents(createTestEvents(testAggregateId, 0, 20));

        List<?> payloads;
        try (var stream = storageWithCountingArchive.readEvents(
                storageWithCountingArchive.createTailToken(), false)) {
            payloads = stream.map(TrackedEventMessage::getPayload).collect(Collectors.toList());
        }

        assertEquals(20, payloads.size());
        verify(archiveStorage, times(3))
                .readEvents(nullable(TrackingToken.class), eq(false));
    }

    @Test
    void trackingStreamUsesTheLargestGapRemovedByOneSourceEvent() {
        long firstTruncatedGap = 1;
        long eventIndex = 5;
        long remainingGap = 8;
        GapAwareTrackingToken startToken = GapAwareTrackingToken.newInstance(
                10,
                List.of(firstTruncatedGap, eventIndex, remainingGap)
        );
        GapAwareTrackingToken sourceEventToken = GapAwareTrackingToken.newInstance(
                10,
                List.of(remainingGap)
        );
        TrackedEventMessage<String> sourceEvent = new GenericTrackedEventMessage<>(
                sourceEventToken,
                new GenericEventMessage<>("GapEvent")
        );
        EventStorageEngine activeStorage = mock(EventStorageEngine.class);
        EventStorageEngine archiveStorage = mock(EventStorageEngine.class);
        AtomicBoolean eventReturned = new AtomicBoolean();
        doAnswer(invocation -> eventReturned.compareAndSet(false, true)
                ? Stream.of(sourceEvent)
                : Stream.empty())
                .when(activeStorage).readEvents(nullable(TrackingToken.class), eq(false));
        doAnswer(invocation -> Stream.empty())
                .when(archiveStorage).readEvents(nullable(TrackingToken.class), eq(false));
        TieredEventStorageEngine storageWithTokenDoubles = new TieredEventStorageEngine(
                activeStorage,
                archiveStorage,
                Duration.ofDays(1),
                activeSpringTransactionManager,
                activeDataSource,
                archiveDataSource,
                2
        );

        TrackedEventMessage<?> event;
        try (var stream = storageWithTokenDoubles.readEvents(startToken, false)) {
            event = stream.findFirst().orElseThrow();
        }

        GapAwareTrackingToken emittedToken = (GapAwareTrackingToken) event.trackingToken();
        assertEquals("GapEvent", event.getPayload());
        assertEquals(10, emittedToken.getIndex());
        assertEquals(Set.of(firstTruncatedGap, remainingGap), emittedToken.getGaps());
    }

    @Test
    void tokenFactoriesUseTheStoreContainingTheRelevantBoundary() {
        Instant archiveStart = Instant.parse("2000-01-01T00:00:00Z");
        Instant activeStart = Instant.parse("2100-01-01T00:00:00Z");
        tieredStorage.appendEvents(createEvents(testAggregateId, 0, 1, "OldEvent", archiveStart));
        tieredStorage.appendEvents(createEvents(testAggregateId, 1, 1, "OldEvent", archiveStart.plusSeconds(2)));
        tieredStorage.appendEvents(createEvents(testAggregateId, 2, 1, "Event", activeStart));
        tieredStorage.appendEvents(createEvents(testAggregateId, 3, 1, "Event", activeStart.plusSeconds(2)));
        tieredStorage.archiveEvents();

        assertEquals(tieredStorage.getArchiveStorage().createTailToken(), tieredStorage.createTailToken());
        assertEquals(tieredStorage.getActiveStorage().createHeadToken(), tieredStorage.createHeadToken());

        List<Long> archiveIndices = archiveJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class);
        List<Long> activeIndices = activeJdbcTemplate.queryForList(
                "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class);

        assertEquals(archiveIndices.get(0) - 1,
                tokenPosition(tieredStorage.createTokenAt(archiveStart.minusSeconds(1))));
        assertEquals(archiveIndices.get(0),
                tokenPosition(tieredStorage.createTokenAt(archiveStart.plusSeconds(1))));
        assertEquals(archiveIndices.get(1),
                tokenPosition(tieredStorage.createTokenAt(activeStart)));
        assertEquals(activeIndices.get(0),
                tokenPosition(tieredStorage.createTokenAt(activeStart.plusSeconds(1))));
        assertEquals(activeIndices.get(1),
                tokenPosition(tieredStorage.createTokenAt(activeStart.plusSeconds(3))));
    }

    @Test
    void createTokenAtUsesTheUnifiedBoundaryWhenThereIsAGapBeforeTheFirstActiveEvent() {
        Instant archiveStart = Instant.parse("2000-01-01T00:00:00Z");
        Instant activeStart = Instant.parse("2100-01-01T00:00:00Z");
        tieredStorage.appendEvents(createEvents(testAggregateId, 0, 2, "OldEvent", archiveStart));
        tieredStorage.appendEvents(createEvents(testAggregateId, 2, 1, "Event", activeStart));
        tieredStorage.appendEvents(createEvents(testAggregateId, 3, 1, "Event", activeStart.plusSeconds(1)));
        tieredStorage.archiveEvents();

        Long removedGlobalIndex = activeJdbcTemplate.queryForObject(
                "SELECT MIN(globalIndex) FROM DomainEventEntry", Long.class);
        activeJdbcTemplate.update(
                "DELETE FROM DomainEventEntry WHERE globalIndex = ?", removedGlobalIndex);
        Long firstActiveGlobalIndex = activeJdbcTemplate.queryForObject(
                "SELECT MIN(globalIndex) FROM DomainEventEntry", Long.class);

        assertEquals(
                firstActiveGlobalIndex - 1,
                tokenPosition(tieredStorage.createTokenAt(activeStart.plusSeconds(1)))
        );
    }

    @Test
    void archivingMovesOnlyTheContinuousGlobalIndexPrefix() {
        Instant activeTimestamp = Instant.now();
        Instant oldTimestamp = activeTimestamp.minus(Duration.ofDays(2));
        tieredStorage.appendEvents(createEvents(testAggregateId, 0, 1, "OldEvent", oldTimestamp));
        tieredStorage.appendEvents(createEvents(testAggregateId, 1, 1, "Event", activeTimestamp));
        tieredStorage.appendEvents(createEvents(testAggregateId, 2, 1, "OldEvent", oldTimestamp));

        tieredStorage.archiveEvents();

        assertEquals(1, countEventsInArchiveStorage(testAggregateId));
        assertEquals(2, countEventsInActiveStorage(testAggregateId));
        assertEquals(
                List.of("OldEvent0", "Event1", "OldEvent2"),
                collectEvents(tieredStorage.readEvents(testAggregateId)).stream()
                        .map(DomainEventMessage::getPayload)
                        .collect(Collectors.toList())
        );

        TrackingToken tailToken = tieredStorage.createTailToken();
        try (var stream = tieredStorage.readEvents(tailToken, false)) {
            assertEquals(
                    List.of("OldEvent0", "Event1", "OldEvent2"),
                    stream.map(TrackedEventMessage::getPayload).collect(Collectors.toList())
            );
        }
    }

    @Test
    void archivingDoesNotMoveEventsAppendedAfterItsBoundaryWasCalculated() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 1));
        Long initialGlobalIndex = activeJdbcTemplate.queryForObject(
                "SELECT globalIndex FROM DomainEventEntry", Long.class);
        AtomicBoolean eventsAppended = new AtomicBoolean();
        PlatformTransactionManager appendingTransactionManager = new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                if (eventsAppended.compareAndSet(false, true)) {
                    tieredStorage.appendEvents(createTestEvents(testAggregateId, 1, 1));
                    tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 2, 1));
                }
                return activeSpringTransactionManager.getTransaction(definition);
            }

            @Override
            public void commit(TransactionStatus status) {
                activeSpringTransactionManager.commit(status);
            }

            @Override
            public void rollback(TransactionStatus status) {
                activeSpringTransactionManager.rollback(status);
            }
        };
        TieredEventStorageEngine storageWithAppendingTransaction = new TieredEventStorageEngine(
                tieredStorage.getActiveStorage(),
                tieredStorage.getArchiveStorage(),
                Duration.ofDays(1),
                appendingTransactionManager,
                activeDataSource,
                archiveDataSource,
                2
        );

        storageWithAppendingTransaction.archiveEvents();

        assertEquals(
                List.of("OldEvent0"),
                collectEvents(storageWithAppendingTransaction.getArchiveStorage()
                        .readEvents(testAggregateId)).stream()
                        .map(DomainEventMessage::getPayload)
                        .collect(Collectors.toList())
        );
        assertEquals(
                List.of("Event1", "OldEvent2"),
                collectEvents(storageWithAppendingTransaction.getActiveStorage()
                        .readEvents(testAggregateId)).stream()
                        .map(DomainEventMessage::getPayload)
                        .collect(Collectors.toList())
        );
        assertEquals(
                List.of(initialGlobalIndex),
                archiveJdbcTemplate.queryForList(
                        "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class)
        );
        assertEquals(
                List.of(initialGlobalIndex + 1, initialGlobalIndex + 2),
                activeJdbcTemplate.queryForList(
                        "SELECT globalIndex FROM DomainEventEntry ORDER BY globalIndex", Long.class)
        );
    }

    @Test
    void archivingCanResumeWhenTheArchiveAlreadyContainsPartOfTheBatch() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 3));
        copyFirstActiveEventToArchive();

        tieredStorage.archiveEvents();

        assertEquals(0, countEventsInActiveStorage(testAggregateId));
        assertEquals(3, countEventsInArchiveStorage(testAggregateId));
        assertEquals(
                List.of("OldEvent0", "OldEvent1", "OldEvent2"),
                collectEvents(tieredStorage.readEvents(testAggregateId)).stream()
                        .map(DomainEventMessage::getPayload)
                        .collect(Collectors.toList())
        );
    }

    @Test
    void archivingFailsWithoutDeletingActiveEventsWhenGlobalIndexBelongsToAnotherEvent() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 3));
        copyFirstActiveEventToArchive();
        String conflictingEventId = UUID.randomUUID().toString();
        archiveJdbcTemplate.update(
                "UPDATE DomainEventEntry SET eventIdentifier = ?", conflictingEventId);

        assertThrows(RuntimeException.class, tieredStorage::archiveEvents);

        assertEquals(3, countEventsInActiveStorage(testAggregateId));
        assertEquals(1, countEventsInArchiveStorage(testAggregateId));
        assertEquals(
                conflictingEventId,
                archiveJdbcTemplate.queryForObject(
                        "SELECT eventIdentifier FROM DomainEventEntry", String.class)
        );
    }

    @Test
    void tokenFactoriesReturnNullWhenBothStoresAreEmpty() {
        assertNull(tieredStorage.createTailToken());
        assertNull(tieredStorage.createHeadToken());
        assertNull(tieredStorage.createTokenAt(Instant.now()));

        try (var stream = tieredStorage.readEvents(null, false)) {
            assertEquals(0, stream.count());
        }
    }

    @Test
    void trackingStreamUsesActiveTokensWhenArchiveIsEmpty() {
        tieredStorage.appendEvents(createTestEvents(testAggregateId, 0, 3));

        TrackingToken tailToken = tieredStorage.createTailToken();
        TrackingToken headToken = tieredStorage.createHeadToken();
        assertNotNull(tailToken);
        assertNotNull(headToken);
        assertNotNull(tieredStorage.createTokenAt(Instant.now()));
        List<Object> payloads;
        try (var stream = tieredStorage.readEvents(tailToken, false)) {
            payloads = stream.map(TrackedEventMessage::getPayload).collect(Collectors.toList());
        }

        assertTrue(tailToken.position().orElseThrow() < headToken.position().orElseThrow());
        assertEquals(List.of("Event0", "Event1", "Event2"), payloads);
        assertEquals(0, countEventsInArchiveStorage(testAggregateId));
    }

    @Test
    void trackingStreamUsesArchiveTokensWhenActiveIsEmpty() {
        tieredStorage.appendEvents(createOldTestEvents(testAggregateId, 0, 3));
        tieredStorage.archiveEvents();

        TrackingToken tailToken = tieredStorage.createTailToken();
        TrackingToken headToken = tieredStorage.createHeadToken();
        assertNotNull(tailToken);
        assertNotNull(headToken);
        assertNotNull(tieredStorage.createTokenAt(Instant.now()));
        List<Object> payloads;
        try (var stream = tieredStorage.readEvents(tailToken, false)) {
            payloads = stream.map(TrackedEventMessage::getPayload).collect(Collectors.toList());
        }

        assertTrue(tailToken.position().orElseThrow() < headToken.position().orElseThrow());
        assertEquals(List.of("OldEvent0", "OldEvent1", "OldEvent2"), payloads);
        assertEquals(0, countEventsInActiveStorage(testAggregateId));
    }

    /**
     * アクティブストレージのイベント数を直接DBクエリで取得
     */
    private int countEventsInActiveStorage(String aggregateId) {
        return activeJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DomainEventEntry WHERE aggregateIdentifier = ?",
                Integer.class,
                aggregateId
        );
    }

    /**
     * アーカイブストレージのイベント数を直接DBクエリで取得
     */
    private int countEventsInArchiveStorage(String aggregateId) {
        return archiveJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DomainEventEntry WHERE aggregateIdentifier = ?",
                Integer.class,
                aggregateId
        );
    }

    private List<DomainEventMessage<?>> collectEvents(DomainEventStream stream) {
        List<DomainEventMessage<?>> events = new ArrayList<>();
        while (stream.hasNext()) {
            events.add(stream.next());
        }
        return events;
    }

    private long tokenPosition(TrackingToken token) {
        assertNotNull(token);
        return token.position().orElseThrow();
    }

    private TieredEventStorageEngine archiveOnFirstArchiveRead() {
        JdbcEventStorageEngine archiveStorage = spy(
                (JdbcEventStorageEngine) tieredStorage.getArchiveStorage()
        );
        AtomicReference<TieredEventStorageEngine> storageReference = new AtomicReference<>();
        AtomicBoolean firstArchiveRead = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (firstArchiveRead.compareAndSet(true, false)) {
                storageReference.get().archiveEvents();
                return Stream.empty();
            }
            return invocation.callRealMethod();
        }).when(archiveStorage).readEvents(nullable(TrackingToken.class), eq(false));
        TieredEventStorageEngine storageWithArchiveReadBarrier = new TieredEventStorageEngine(
                tieredStorage.getActiveStorage(),
                archiveStorage,
                Duration.ofDays(1),
                activeSpringTransactionManager,
                activeDataSource,
                archiveDataSource,
                2
        );
        storageReference.set(storageWithArchiveReadBarrier);
        return storageWithArchiveReadBarrier;
    }

    private void copyFirstActiveEventToArchive() {
        Object[] event = activeJdbcTemplate.queryForObject(
                "SELECT globalIndex, aggregateIdentifier, sequenceNumber, type, eventIdentifier, " +
                        "metaData, payload, payloadRevision, payloadType, timeStamp " +
                        "FROM DomainEventEntry ORDER BY globalIndex LIMIT 1",
                (rs, rowNum) -> new Object[] {
                        rs.getLong("globalIndex"),
                        rs.getString("aggregateIdentifier"),
                        rs.getLong("sequenceNumber"),
                        rs.getString("type"),
                        rs.getString("eventIdentifier"),
                        rs.getBytes("metaData"),
                        rs.getBytes("payload"),
                        rs.getString("payloadRevision"),
                        rs.getString("payloadType"),
                        rs.getString("timeStamp")
                }
        );
        archiveJdbcTemplate.update(
                "INSERT INTO DomainEventEntry " +
                        "(globalIndex, aggregateIdentifier, sequenceNumber, type, eventIdentifier, " +
                        "metaData, payload, payloadRevision, payloadType, timeStamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                event
        );
    }

    private List<DomainEventMessage<?>> createTestEvents(String aggregateId, int firstSequenceNumber, int count) {
        return createEvents(aggregateId, firstSequenceNumber, count, "Event", Instant.now());
    }

    private List<DomainEventMessage<?>> createOldTestEvents(String aggregateId,
                                                            int firstSequenceNumber,
                                                            int count) {
        return createEvents(
                aggregateId,
                firstSequenceNumber,
                count,
                "OldEvent",
                Instant.now().minus(Duration.ofDays(2))
        );
    }

    private List<DomainEventMessage<?>> createEvents(String aggregateId,
                                                     int firstSequenceNumber,
                                                     int count,
                                                     String payloadPrefix,
                                                     Instant timestamp) {
        List<DomainEventMessage<?>> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long sequenceNumber = firstSequenceNumber + i;
            String messageId = UUID.randomUUID().toString();
            Map<String, Object> metaData = new HashMap<>();

            result.add(new GenericDomainEventMessage<>(
                    "TestAggregate",
                    aggregateId,
                    sequenceNumber,
                    payloadPrefix + sequenceNumber,
                    metaData,
                    messageId,
                    timestamp));
        }
        return result;
    }
}
