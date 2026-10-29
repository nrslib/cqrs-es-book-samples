// 紙面照合: 4.27
package org.example.cqrsesbooksamples.chapter04._20;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


// Inventoryの実装例
public record Inventory(String productId, int totalQuantity, Map<String, Integer> reservations) {
    public Inventory {
        reservations = Map.copyOf(reservations);
    }

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

    public int reservedQuantity() {
        return reservations.values().stream().mapToInt(Integer::intValue).sum();
    }

    public InventoryEvent reserve(int quantity, String orderId) {
        if (totalQuantity - reservedQuantity() < quantity) {
            return new InventoryReservationFailedEvent(productId, orderId);
        }
        return new InventoryReservedEvent(productId, quantity, orderId);
    }

    public Optional<InventoryReleasedEvent> release(String orderId) {
        var reserved = reservations.get(orderId);
        if (reserved == null) {
            // 予約の記録がない注文（解放済みを含む）は何も起こさない
            return Optional.empty();
        }
        return Optional.of(new InventoryReleasedEvent(productId, reserved, orderId));
    }

    public static Inventory applyEvent(InventoryCreatedEvent event) {
        return new Inventory(event.productId(), event.quantity(), Map.of());
    }

    public Inventory applyEvent(InventoryEvent event) {
        return switch (event) {
            case InventoryCreatedEvent e -> applyEvent(e);
            case InventoryAddedEvent e ->
                new Inventory(productId, totalQuantity + e.quantity(), reservations);
            case InventoryReservedEvent e ->
                new Inventory(productId, totalQuantity, put(reservations, e.orderId(), e.quantity()));
            case InventoryReleasedEvent e ->
                new Inventory(productId, totalQuantity, remove(reservations, e.orderId()));
            default -> this;
        };
    }

    private static Map<String, Integer> put(Map<String, Integer> reservations, String orderId, int quantity) {
        var next = new HashMap<>(reservations);
        next.put(orderId, quantity);
        return next;
    }

    private static Map<String, Integer> remove(Map<String, Integer> reservations, String orderId) {
        var next = new HashMap<>(reservations);
        next.remove(orderId);
        return next;
    }
}
