-- 紙面照合: 4.15, 4.16
-- 参照のたびに結合と集計が走るクエリ
SELECT d.*, COUNT(h.user_id) as helpful_count
FROM documents d
LEFT JOIN helpful_marks h ON d.document_id = h.document_id
GROUP BY d.document_id;

-- 集計済みリードモデルへの問い合わせ
SELECT * FROM document_views;
