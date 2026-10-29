package org.example.cqrsesbooksamples.chapter04._01;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record UnmarkAsHelpfulCommand(String documentId, String userId) {
    @TargetAggregateIdentifier
    public HelpfulId helpfulId() {
        return new HelpfulId(documentId, userId);
    }
}
