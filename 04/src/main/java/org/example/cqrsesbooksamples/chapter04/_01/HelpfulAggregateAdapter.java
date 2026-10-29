// 紙面照合: 4.6, 4.7
package org.example.cqrsesbooksamples.chapter04._01;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class HelpfulAggregateAdapter {
    @AggregateIdentifier
    private HelpfulId helpfulId() {
        return helpful == null ? null : helpful.id();
    }
    private Helpful helpful;

    public HelpfulAggregateAdapter() {
    }


    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public void handle(MarkAsHelpfulCommand command) {
        var helpfulId = new HelpfulId(command.documentId(), command.userId());
        var event = helpful == null
            ? Helpful.create(helpfulId)
            : helpful.toggleHelpful(helpfulId);
        AggregateLifecycle.apply(event);
    }


    @CommandHandler
    public void handle(UnmarkAsHelpfulCommand command) {
        var event = helpful.unmark();
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(HelpfulEvent event) {
        if (helpful != null) {
            helpful = helpful.applyEvent(event);
        } else {
            if (event instanceof HelpfulMarkedEvent marked) {
                helpful = Helpful.applyEvent(
                    new HelpfulId(marked.documentId(), marked.userId()), marked);
            } else {
                throw new IllegalStateException("Expected HelpfulMarkedEvent");
            }
        }
    }
}
