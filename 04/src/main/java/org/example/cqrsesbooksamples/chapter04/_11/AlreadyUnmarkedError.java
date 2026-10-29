package org.example.cqrsesbooksamples.chapter04._11;

import org.example.cqrsesbooksamples.chapter04._01.HelpfulId;

public record AlreadyUnmarkedError(HelpfulId id) implements DomainError {
    @Override
    public String message() {
        return "Helpful is already unmarked: " + id;
    }
}
