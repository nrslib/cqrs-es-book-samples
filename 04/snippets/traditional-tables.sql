-- 紙面照合: 4.14
-- 従来的な構造のテーブル定義
CREATE TABLE documents (
    document_id VARCHAR PRIMARY KEY,
    title VARCHAR,
    content TEXT
);

CREATE TABLE helpful_marks (
    document_id VARCHAR,
    user_id VARCHAR,
    PRIMARY KEY (document_id, user_id)
);
