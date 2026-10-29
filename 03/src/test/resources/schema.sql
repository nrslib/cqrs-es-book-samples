-- DomainEventEntry テーブル（ドメインイベント用）
CREATE TABLE IF NOT EXISTS DomainEventEntry (
                                                globalIndex BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                aggregateIdentifier VARCHAR(255) NOT NULL,
    sequenceNumber BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    eventIdentifier VARCHAR(255) NOT NULL,
    metaData BLOB,
    payload BLOB NOT NULL,
    payloadRevision VARCHAR(255),
    payloadType VARCHAR(255) NOT NULL,
    timeStamp VARCHAR(255) NOT NULL,
    UNIQUE (aggregateIdentifier, sequenceNumber),
    UNIQUE (eventIdentifier)
    );

-- SnapshotEventEntry テーブル（スナップショット用）
CREATE TABLE IF NOT EXISTS SnapshotEventEntry (
                                                  aggregateIdentifier VARCHAR(255) NOT NULL,
    sequenceNumber BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    eventIdentifier VARCHAR(255) NOT NULL,
    metaData BLOB,
    payload BLOB NOT NULL,
    payloadRevision VARCHAR(255),
    payloadType VARCHAR(255) NOT NULL,
    timeStamp VARCHAR(255) NOT NULL,
    PRIMARY KEY (aggregateIdentifier, sequenceNumber),
    UNIQUE (eventIdentifier)
    );

-- TokenEntry テーブル（イベントプロセッサのトークン用）
CREATE TABLE IF NOT EXISTS TokenEntry (
                                          processorName VARCHAR(255) NOT NULL,
    segment INTEGER NOT NULL,
    token BLOB,
    tokenType VARCHAR(255) NOT NULL,
    timestamp VARCHAR(255) NOT NULL,
    owner VARCHAR(255),
    PRIMARY KEY (processorName, segment)
    );

-- SagaEntry テーブル（サガ状態用）
CREATE TABLE IF NOT EXISTS SagaEntry (
                                         sagaId VARCHAR(255) NOT NULL,
    revision VARCHAR(255),
    sagaType VARCHAR(255),
    serializedSaga BLOB,
    PRIMARY KEY (sagaId)
    );

-- AssociationValueEntry テーブル（サガの関連付け値用）
CREATE TABLE IF NOT EXISTS AssociationValueEntry (
                                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     associationKey VARCHAR(255) NOT NULL,
    associationValue VARCHAR(255) NOT NULL,
    sagaId VARCHAR(255) NOT NULL,
    sagaType VARCHAR(255) NOT NULL
    );

-- インデックスの作成
CREATE INDEX IF NOT EXISTS idx_association_value_entry ON AssociationValueEntry (sagaType, associationKey, associationValue);
CREATE INDEX IF NOT EXISTS idx_saga_id ON AssociationValueEntry (sagaId);