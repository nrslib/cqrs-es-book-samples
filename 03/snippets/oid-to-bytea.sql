-- 紙面照合: 3.25
-- OID型からBYTEA型へ移行するSQL
-- 新しいBYTEAカラムを追加
ALTER TABLE token_entry ADD COLUMN token_bytea BYTEA;

-- Large Objectからデータをコピー
UPDATE token_entry SET token_bytea = lo_get(token);

-- カラムを消す前にLarge Object本体を削除する
SELECT lo_unlink(token) FROM token_entry WHERE token IS NOT NULL;

-- 古いカラムを削除して新しいカラムをリネーム
ALTER TABLE token_entry DROP COLUMN token;
ALTER TABLE token_entry RENAME COLUMN token_bytea TO token;
