package org.example.cqrsesbooksamples.chapter10._11;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DocumentViewRepository extends JpaRepository<DocumentView, String> {
    // flushAutomatically: 同じバッチ内のsave（行の新規作成）をUPDATE実行前にDBへ反映させる
    @Modifying(flushAutomatically = true)
    @Query("UPDATE DocumentView v SET v.helpfulCount = v.helpfulCount + :delta WHERE v.documentId = :documentId")
    int addToHelpfulCount(String documentId, int delta);
}
