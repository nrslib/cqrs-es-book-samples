package org.example.cqrsesbooksamples.chapter03._16;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * イベントアーカイバ
 * 定期的に古いイベントをアクティブストレージからアーカイブストレージに移動する
 */
@Component
@EnableScheduling
public class EventArchiver {
    private static final Logger logger = LoggerFactory.getLogger(EventArchiver.class);

    private final TieredEventStorageEngine storageEngine;

    public EventArchiver(TieredEventStorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    /**
     * 古いイベントをアーカイブする定期実行ジョブ
     * デフォルトでは毎日午前1時に実行
     */
    @Scheduled(cron = "${axon.event-archiver.cron:0 0 1 * * ?}")
    public void archiveOldEvents() {
        logger.info("Starting scheduled event archiving job");
        try {
            storageEngine.archiveEvents();
            logger.info("Scheduled event archiving completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled event archiving", e);
        }
    }

    /**
     * 手動でアーカイブ処理を実行するメソッド
     * 管理APIなどから呼び出すことができる
     *
     * @return アーカイブ処理の結果
     */
    public boolean manualArchive() {
        logger.info("Starting manual event archiving");
        try {
            storageEngine.archiveEvents();
            logger.info("Manual event archiving completed successfully");
            return true;
        } catch (Exception e) {
            logger.error("Error during manual event archiving", e);
            return false;
        }
    }
}