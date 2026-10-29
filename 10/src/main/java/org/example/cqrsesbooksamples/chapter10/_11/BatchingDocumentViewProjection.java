// 紙面照合: 10.7
package org.example.cqrsesbooksamples.chapter10._11;

import java.util.LinkedHashMap;
import java.util.Map;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.unitofwork.CurrentUnitOfWork;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.springframework.stereotype.Component;


@Component
@ProcessingGroup("document-view")
public class BatchingDocumentViewProjection {
    private static final String PENDING_COUNTS = "pendingHelpfulCounts";

    private final DocumentViewRepository repository;

    public BatchingDocumentViewProjection(DocumentViewRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(DocumentCreatedEvent event) {
        repository.save(new DocumentView(event.documentId(), 0));
    }

    @EventHandler
    public void on(HelpfulMarkedEvent event) {
        addPending(event.documentId(), 1);
    }

    @EventHandler
    public void on(HelpfulUnmarkedEvent event) {
        addPending(event.documentId(), -1);
    }

    // すぐにUPDATEせず、バッチ内の増減をドキュメントIDごとに積んでおく
    private void addPending(String documentId, int delta) {
        UnitOfWork<?> unitOfWork = CurrentUnitOfWork.get().root();
        Map<String, Integer> pending = unitOfWork.getOrComputeResource(PENDING_COUNTS, key -> {
            var counts = new LinkedHashMap<String, Integer>();
            // コミット直前（トークン更新と同じトランザクション内）にまとめて書き出す
            unitOfWork.onPrepareCommit(u -> flush(counts));
            return counts;
        });
        pending.merge(documentId, delta, Integer::sum);
    }

    private void flush(Map<String, Integer> pending) {
        pending.forEach((documentId, delta) -> {
            int updated = repository.addToHelpfulCount(documentId, delta);
            if (updated == 0) {
                throw new IllegalStateException("DocumentView not found: " + documentId);
            }
        });
    }
}
