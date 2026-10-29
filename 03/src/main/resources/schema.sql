-- DomainEventEntry テーブル（ドメインイベント用）
CREATE TABLE IF NOT EXISTS DomainEventEntry (
                                                global_index BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_identifier VARCHAR(255) NOT NULL,
    meta_data BLOB,
    payload BLOB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    UNIQUE (aggregate_identifier, sequence_number),
    UNIQUE (event_identifier)
    );

-- SnapshottingEvent テーブル（スナップショット用）
CREATE TABLE IF NOT EXISTS SnapshotEventEntry (
                                                  aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_identifier VARCHAR(255) NOT NULL,
    meta_data BLOB,
    payload BLOB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    PRIMARY KEY (aggregate_identifier, sequence_number),
    UNIQUE (event_identifier)
    );

-- TokenEntry テーブル（イベントプロセッサのトークン用）
CREATE TABLE IF NOT EXISTS TokenEntry (
                                          processor_name VARCHAR(255) NOT NULL,
    segment INTEGER NOT NULL,
    token BLOB,
    token_type VARCHAR(255) NOT NULL,
    timestamp VARCHAR(255) NOT NULL,
    owner VARCHAR(255),
    PRIMARY KEY (processor_name, segment)
    );

-- SagaEntry テーブル（サガ状態用）
CREATE TABLE IF NOT EXISTS SagaEntry (
                                         saga_id VARCHAR(255) NOT NULL,
    revision VARCHAR(255),
    saga_type VARCHAR(255),
    serialized_saga BLOB,
    PRIMARY KEY (saga_id)
    );

-- AssociationValueEntry テーブル（サガの関連付け値用）
CREATE TABLE IF NOT EXISTS AssociationValueEntry (
                                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     association_key VARCHAR(255) NOT NULL,
    association_value VARCHAR(255) NOT NULL,
    saga_id VARCHAR(255) NOT NULL,
    saga_type VARCHAR(255) NOT NULL
    );

-- インデックスの作成
CREATE INDEX IF NOT EXISTS idx_association_value_entry ON AssociationValueEntry (saga_type, association_key, association_value);
CREATE INDEX IF NOT EXISTS idx_saga_id ON AssociationValueEntry (saga_id);
-- 第3章 _33: 個人情報ストア（参照IDパターン）
CREATE TABLE IF NOT EXISTS personal_information (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(1024),
    phone_number VARCHAR(1024),
    address VARCHAR(1024),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
