// 紙面照合: 4.10
package org.example.cqrsesbooksamples.chapter04._11;

import org.example.cqrsesbooksamples.chapter04._01.HelpfulId;
import org.example.cqrsesbooksamples.chapter04._01.HelpfulUnmarkedEvent;

import io.vavr.control.Either;


public record Helpful(HelpfulId id, boolean marked) {
    public Either<DomainError, HelpfulUnmarkedEvent> unmark() {
        if (!marked) {
            return Either.left(new AlreadyUnmarkedError(id));
        }
        return Either.right(new HelpfulUnmarkedEvent(id.documentId(), id.userId()));
    }
}
