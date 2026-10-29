// 紙面照合: 4.18
package org.example.cqrsesbooksamples.chapter04._01;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;


@Component
public class DocumentQueryHandler {
    private final DocumentViewRepository repository;

    public DocumentQueryHandler(DocumentViewRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public DocumentView handle(FindDocumentViewQuery query) {
        return repository.findById(query.documentId())
            .orElseThrow();
    }
}
