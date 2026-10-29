-- 紙面照合: 3.13
-- トラッキングトークンを保存するテーブル定義
-- PostgreSQL方言でAxonのJPAエンティティからスキーマ生成した結果
-- (検証: chapter02._55.DomainEventEntryDdlTest)
-- TokenStoreに保存されるToken情報（PostgreSQL向けに自動生成された定義）
create table token_entry (
    processor_name varchar(255) not null,  -- "user-projection"
    segment        integer not null,       -- セグメント番号: 0, 1, 2, ...
    owner          varchar(255),           -- "instance-1" のような所有者情報
    timestamp      varchar(255) not null,  -- クレームの時刻（文字列で保持される）
    token          oid,                    -- 処理位置情報（BYTEA設定後はbytea）
    token_type     varchar(255),
    primary key (processor_name, segment)
);
