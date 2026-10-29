-- 紙面照合: 2.44
-- domain_event_entryテーブルの定義
-- PostgreSQL方言でAxonのJPAエンティティからスキーマ生成した結果
-- (検証: chapter02._55.DomainEventEntryDdlTest)
create sequence domain_event_entry_seq start with 1 increment by 50;

create table domain_event_entry (
    global_index         bigint not null,
    event_identifier     varchar(255) not null unique,
    meta_data            oid,
    payload              oid not null,
    payload_revision     varchar(255),
    payload_type         varchar(255) not null,
    time_stamp           varchar(255) not null,
    aggregate_identifier varchar(255) not null,
    sequence_number      bigint not null,
    type                 varchar(255),
    primary key (global_index),
    unique (aggregate_identifier, sequence_number)
);
