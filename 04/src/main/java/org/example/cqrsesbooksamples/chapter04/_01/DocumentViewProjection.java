// 紙面照合: 4.13
package org.example.cqrsesbooksamples.chapter04._01;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
public class DocumentViewProjection {
    private final DocumentViewRepository repository;

    public DocumentViewProjection(DocumentViewRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(DocumentCreatedEvent event) {
        var view = new DocumentView(
            event.documentId(),
            event.title(),
            event.content(),
            0
        );
        repository.save(view);
    }

    @EventHandler
    public void on(HelpfulMarkedEvent event) {
        int updated = repository.incrementHelpfulCount(event.documentId());
        if (updated == 0) {
            throw new IllegalStateException("DocumentView not found: " + event.documentId());
        }
    }

    @EventHandler
    public void on(HelpfulUnmarkedEvent event) {
        int updated = repository.decrementHelpfulCount(event.documentId());
        if (updated == 0) {
            throw new IllegalStateException("DocumentView not found: " + event.documentId());
        }
    }
}
