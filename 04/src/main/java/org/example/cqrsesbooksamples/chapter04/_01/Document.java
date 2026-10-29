// 紙面照合: 4.2
package org.example.cqrsesbooksamples.chapter04._01;


public record Document(String documentId) {
    public static DocumentCreatedEvent create(String documentId, String title, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        return new DocumentCreatedEvent(documentId, title, content);
    }

    public static Document applyEvent(DocumentCreatedEvent event) {
        return new Document(event.documentId());
    }

    public Document applyEvent(DocumentEvent event) {
        return switch (event) {
            case DocumentCreatedEvent e -> applyEvent(e);
            default -> throw new IllegalStateException("Unknown event type: " + event);
        };
    }
}
