// 検証補助: 2.44, 3.13
package org.example.cqrsesbooksamples.chapter02._55;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;

import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.DelayedDropRegistryNotAvailableImpl;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.junit.jupiter.api.Test;

/**
 * リスト2.44(domain_event_entry)・リスト3.13(token_entry)の転記を検証する。
 * Spring Boot既定の命名戦略+PostgreSQL方言でAxonのJPAエンティティから
 * スキーマを生成し、書籍に転記したDDLの列名・型が実際の生成結果と
 * 一致することを確かめる。
 */
class DomainEventEntryDdlTest {


    @Test
    void PostgreSQL方言で生成したdomain_event_entryのDDLが転記と一致する() throws Exception {
        String ddl = exportPostgresDdl();
        System.out.println(ddl);

        assertThat(ddl).contains("create table domain_event_entry");
        // 主キーはグローバルインデックス
        assertThat(ddl).contains("global_index bigint not null");
        assertThat(ddl).contains("primary key (global_index)");
        // 集約ID+シーケンス番号のユニーク制約
        assertThat(ddl).containsPattern("unique \\(aggregate_identifier, sequence_number\\)");
        // 列名はsnake_case。時刻列はtime_stamp(文字列)
        assertThat(ddl).contains("event_identifier varchar(255) not null");
        assertThat(ddl).contains("time_stamp varchar(255) not null");
        assertThat(ddl).contains("payload_type varchar(255) not null");
        assertThat(ddl).contains("payload_revision varchar(255)");
        assertThat(ddl).contains("type varchar(255)");
        // @Lobのbyte[]はPostgreSQLではoidになる(BYTEA設定の話は第3章)
        assertThat(ddl).contains("payload oid not null");
        assertThat(ddl).contains("meta_data oid");
    }


    @Test
    void PostgreSQL方言で生成したtoken_entryのDDLが転記と一致する() throws Exception {
        String ddl = exportPostgresDdl();

        assertThat(ddl).contains("create table token_entry");
        assertThat(ddl).contains("processor_name varchar(255) not null");
        assertThat(ddl).contains("segment integer not null");
        assertThat(ddl).contains("token oid");
        assertThat(ddl).contains("token_type varchar(255)");
        // 時刻列の実体は文字列であってDATETIMEではない
        assertThat(ddl).contains("timestamp varchar(255) not null");
        assertThat(ddl).contains("owner varchar(255)");
        assertThat(ddl).contains("primary key (processor_name, segment)");
    }

    private String exportPostgresDdl() throws Exception {
        var registry = new StandardServiceRegistryBuilder()
            .applySettings(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                "hibernate.implicit_naming_strategy",
                "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy"
            ))
            .build();
        try {
            Metadata metadata = new MetadataSources(registry)
                .addAnnotatedClass(DomainEventEntry.class)
                .addAnnotatedClass(TokenEntry.class)
                .buildMetadata();

            Path out = Files.createTempFile("postgres-schema", ".sql");
            SchemaManagementToolCoordinator.process(
                metadata,
                (org.hibernate.service.ServiceRegistry) registry,
                Map.of(
                    "jakarta.persistence.schema-generation.scripts.action", "create",
                    "jakarta.persistence.schema-generation.scripts.create-target", out.toString(),
                    "hibernate.hbm2ddl.delimiter", ";"
                ),
                DelayedDropRegistryNotAvailableImpl.INSTANCE
            );
            return Files.readString(out);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
