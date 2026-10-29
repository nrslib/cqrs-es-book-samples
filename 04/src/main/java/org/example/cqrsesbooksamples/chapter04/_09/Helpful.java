package org.example.cqrsesbooksamples.chapter04._09;

import org.example.cqrsesbooksamples.chapter04._01.HelpfulId;
import org.example.cqrsesbooksamples.chapter04._01.HelpfulUnmarkedEvent;

// 削除済み（旧 4.10）
public record Helpful(HelpfulId id, boolean marked) {
    public HelpfulUnmarkedEvent unmark() {
        if (!marked) {
            throw new HelpfulDomainException.AlreadyUnmarkedException(id);
        }
        return new HelpfulUnmarkedEvent(id.documentId(), id.userId());
    }
}
