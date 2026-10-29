// 紙面照合: 4.9
package org.example.cqrsesbooksamples.chapter04._09;

import org.example.cqrsesbooksamples.chapter04._01.HelpfulId;


public sealed class HelpfulDomainException extends RuntimeException {
    private HelpfulDomainException(String message) {
        super(message);
    }

    public static final class AlreadyUnmarkedException extends HelpfulDomainException {
        public AlreadyUnmarkedException(HelpfulId id) {
            super("Helpful is already unmarked: " + id);
        }
    }

    public static final class InvalidStateException extends HelpfulDomainException {
        public InvalidStateException(String message) {
            super(message);
        }
    }
}
