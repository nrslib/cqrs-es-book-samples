// 紙面照合: 4.8
package org.example.cqrsesbooksamples.chapter04._01;


public record Helpful(HelpfulId id, boolean marked) {
    public static HelpfulMarkedEvent create(HelpfulId id) {
        return new HelpfulMarkedEvent(id.documentId(), id.userId());
    }

    public HelpfulEvent toggleHelpful(HelpfulId id) {
        return marked
            ? new HelpfulUnmarkedEvent(id.documentId(), id.userId())
            : new HelpfulMarkedEvent(id.documentId(), id.userId());
    }

    public HelpfulUnmarkedEvent unmark() {
        if (!marked) {
            throw new IllegalStateException("Already unmarked");
        }
        return new HelpfulUnmarkedEvent(id.documentId(), id.userId());
    }

    public static Helpful applyEvent(HelpfulId id, HelpfulMarkedEvent event) {
        return new Helpful(id, true);
    }

    public Helpful applyEvent(HelpfulEvent event) {
        return switch (event) {
            case HelpfulMarkedEvent _ -> new Helpful(id, true);
            case HelpfulUnmarkedEvent _ -> new Helpful(id, false);
            default -> throw new IllegalStateException("Unknown event type: " + event);
        };
    }
}
