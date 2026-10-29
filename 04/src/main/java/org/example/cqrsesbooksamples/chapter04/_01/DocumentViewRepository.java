// 紙面照合: 4.13
package org.example.cqrsesbooksamples.chapter04._01;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface DocumentViewRepository extends JpaRepository<DocumentView, String> {
    @Modifying
    @Query("UPDATE DocumentView v SET v.helpfulCount = v.helpfulCount + 1 WHERE v.documentId = :documentId")
    int incrementHelpfulCount(String documentId);

    @Modifying
    @Query("UPDATE DocumentView v SET v.helpfulCount = v.helpfulCount - 1 WHERE v.documentId = :documentId")
    int decrementHelpfulCount(String documentId);
}
