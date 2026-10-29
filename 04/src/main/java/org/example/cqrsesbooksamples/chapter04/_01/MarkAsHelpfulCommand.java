// 紙面照合: 4.6
package org.example.cqrsesbooksamples.chapter04._01;

import org.axonframework.modelling.command.TargetAggregateIdentifier;


public record MarkAsHelpfulCommand(String documentId, String userId) {
    @TargetAggregateIdentifier
    public HelpfulId helpfulId() {
        return new HelpfulId(documentId, userId);
    }
}
