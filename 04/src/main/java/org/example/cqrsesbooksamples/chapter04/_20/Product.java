// 紙面照合: 4.19
package org.example.cqrsesbooksamples.chapter04._20;

import java.math.BigDecimal;


public record Product(String id) {
    public static ProductCreatedEvent create(String id, String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        return new ProductCreatedEvent(id, name, price);
    }

    public static Product applyEvent(ProductCreatedEvent event) {
        return new Product(event.productId());
    }

    public Product applyEvent(ProductEvent event) {
        return switch (event) {
            case ProductCreatedEvent e -> applyEvent(e);
            default -> throw new IllegalStateException("Unknown event type: " + event);
        };
    }
}
