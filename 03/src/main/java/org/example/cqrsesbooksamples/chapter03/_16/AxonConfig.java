package org.example.cqrsesbooksamples.chapter03._16;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.jdbc.SpringDataSourceConnectionProvider;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
public class AxonConfig {

    /**
     * アクティブストレージ用のデータソース
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.active")
    public DataSource activeDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * アーカイブストレージ用のデータソース
     */
    @Bean
    @Qualifier("archiveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.archive")
    public DataSource archiveDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * アクティブストレージ用のSpringトランザクションマネージャ
     */
    @Bean
    @Primary
    public PlatformTransactionManager activeSpringTransactionManager(DataSource activeDataSource) {
        return new DataSourceTransactionManager(activeDataSource);
    }

    /**
     * アクティブストレージ用のAxonトランザクションマネージャ（Springのトランザクションマネージャをラップ）
     */
    @Bean
    @Primary
    public TransactionManager activeTransactionManager(PlatformTransactionManager activeSpringTransactionManager) {
        return new SpringTransactionManager(activeSpringTransactionManager);
    }

    /**
     * アーカイブストレージ用のSpringトランザクションマネージャ
     */
    @Bean
    @Qualifier("archiveSpringTransactionManager")
    public PlatformTransactionManager archiveSpringTransactionManager(@Qualifier("archiveDataSource") DataSource archiveDataSource) {
        return new DataSourceTransactionManager(archiveDataSource);
    }

    /**
     * アーカイブストレージ用のAxonトランザクションマネージャ（Springのトランザクションマネージャをラップ）
     */
    @Bean
    @Qualifier("archiveTransactionManager")
    public TransactionManager archiveTransactionManager(@Qualifier("archiveSpringTransactionManager") PlatformTransactionManager archiveSpringTransactionManager) {
        return new SpringTransactionManager(archiveSpringTransactionManager);
    }

    /**
     * イベントスキーマの設定
     */
    @Bean
    public EventSchema eventSchema() {
        return EventSchema.builder()
                .eventTable("DomainEventEntry")
                .snapshotTable("SnapshotEventEntry")
                .build();
    }

    /**
     * シリアライザの設定
     */
    @Primary
    @Bean
    public Serializer serializer() {
        return JacksonSerializer.builder().build();
    }

    /**
     * アクティブストレージ用のJDBCイベントストレージエンジン
     */
    @Bean
    public JdbcEventStorageEngine activeStorageEngine(
            DataSource activeDataSource,
            Serializer serializer,
            TransactionManager activeTransactionManager,
            EventSchema eventSchema) {

        return JdbcEventStorageEngine.builder()
                .connectionProvider(new SpringDataSourceConnectionProvider(activeDataSource))
                .transactionManager(activeTransactionManager)
                .eventSerializer(serializer)
                .snapshotSerializer(serializer)
                .schema(eventSchema)
                .build();
    }

    /**
     * アーカイブストレージ用のJDBCイベントストレージエンジン
     */
    @Bean
    public JdbcEventStorageEngine archiveStorageEngine(
            @Qualifier("archiveDataSource") DataSource archiveDataSource,
            Serializer serializer,
            @Qualifier("archiveTransactionManager") TransactionManager archiveTransactionManager,
            EventSchema eventSchema) {

        return JdbcEventStorageEngine.builder()
                .connectionProvider(new SpringDataSourceConnectionProvider(archiveDataSource))
                .transactionManager(archiveTransactionManager)
                .eventSerializer(serializer)
                .snapshotSerializer(serializer)
                .schema(eventSchema)
                .build();
    }

    /**
     * 階層型ストレージエンジン
     */
    @Bean
    public TieredEventStorageEngine eventStorageEngine(
            JdbcEventStorageEngine activeStorageEngine,
            JdbcEventStorageEngine archiveStorageEngine,
            PlatformTransactionManager activeSpringTransactionManager,
            DataSource activeDataSource,
            @Qualifier("archiveDataSource") DataSource archiveDataSource) {

        // アーカイブ閾値の設定（例：30日）
        Duration archiveThreshold = Duration.ofDays(30);

        // バッチサイズの設定（例：100件ずつ処理）
        int batchSize = 100;

        return new TieredEventStorageEngine(
                activeStorageEngine,
                archiveStorageEngine,
                archiveThreshold,
                activeSpringTransactionManager,
                activeDataSource,
                archiveDataSource,
                batchSize);
    }

    /**
     * イベントストアの設定
     */
    @Bean
    public EventStore eventStore(EventStorageEngine eventStorageEngine) {
        return EmbeddedEventStore.builder()
                .storageEngine(eventStorageEngine)
                .build();
    }

    /**
     * イベントアーカイバの設定
     */
    @Bean
    public EventArchiver eventArchiver(TieredEventStorageEngine eventStorageEngine) {
        return new EventArchiver(eventStorageEngine);
    }
}