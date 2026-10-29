// 紙面照合: 4.21
package org.example.cqrsesbooksamples.chapter04._22;


public record Inventory(String productId, int quantity) {
    public static InventoryCreatedEvent create(String productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative");
        }
        return new InventoryCreatedEvent(productId, quantity);
    }

    public InventoryAddedEvent add(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Added quantity must be positive");
        }
        return new InventoryAddedEvent(productId, quantity);
    }

    public static Inventory applyEvent(InventoryCreatedEvent event) {
        return new Inventory(event.productId(), event.quantity());
    }

    public Inventory applyEvent(InventoryEvent event) {
        return switch (event) {
            case InventoryCreatedEvent e -> applyEvent(e);
            case InventoryAddedEvent e ->
                new Inventory(productId, quantity + e.quantity());
            default -> throw new IllegalStateException("Unknown event type: " + event);
        };
    }
}
