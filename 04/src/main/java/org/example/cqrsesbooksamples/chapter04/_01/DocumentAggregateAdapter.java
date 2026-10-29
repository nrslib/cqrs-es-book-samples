// 紙面照合: 4.1
package org.example.cqrsesbooksamples.chapter04._01;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class DocumentAggregateAdapter {
    @AggregateIdentifier
    private String documentId() {
        return document == null ? null : document.documentId();
    }
    private Document document;

    public DocumentAggregateAdapter() {
    }

    @CommandHandler
    public DocumentAggregateAdapter(CreateDocumentCommand command) {
        var documentId = UUID.randomUUID().toString();
        var event = Document.create(
            documentId,
            command.title(),
            command.content()
        );
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(DocumentEvent event) {
        if (document != null) {
            document = document.applyEvent(event);
        } else {
            if (event instanceof DocumentCreatedEvent e) {
                document = Document.applyEvent(e);
            } else {
                throw new IllegalStateException("Expected DocumentCreatedEvent");
            }
        }
    }
}
