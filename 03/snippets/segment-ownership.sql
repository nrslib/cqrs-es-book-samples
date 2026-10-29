-- 紙面照合: 3.16
-- セグメントの所有インスタンスを確認するクエリ
SELECT segment, owner FROM token_entry
WHERE processor_name = 'user-projection';
