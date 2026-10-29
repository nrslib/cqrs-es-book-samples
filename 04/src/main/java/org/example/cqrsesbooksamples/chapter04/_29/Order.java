// 紙面照合: 4.25
package org.example.cqrsesbooksamples.chapter04._29;

import java.util.List;
import java.util.LinkedHashMap;


public record Order(String id, OrderStatus status, List<OrderLine> orderLines) {
    public static OrderCreatedEvent create(String id, List<OrderLine> orderLines) {
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line");
        }
        var quantities = new LinkedHashMap<String, Integer>();
        for (var line : orderLines) {
            quantities.merge(line.productId(), line.quantity(), Math::addExact);
        }
        var combinedLines = quantities.entrySet().stream()
            .map(entry -> new OrderLine(entry.getKey(), entry.getValue()))
            .toList();
        return new OrderCreatedEvent(id, combinedLines);
    }

    public OrderCompletedEvent complete() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be completed");
        }
        return new OrderCompletedEvent(id);
    }

    public OrderCancelledEvent cancel() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }
        return new OrderCancelledEvent(id);
    }

    public static Order applyEvent(OrderCreatedEvent event) {
        return new Order(
            event.orderId(),
            OrderStatus.PENDING,
            List.copyOf(event.orderLines())
        );
    }

    public Order applyEvent(OrderEvent event) {
        return switch (event) {
            case OrderCreatedEvent e -> applyEvent(e);
            case OrderCompletedEvent _ -> new Order(id, OrderStatus.COMPLETED, orderLines);
            case OrderCancelledEvent _ -> new Order(id, OrderStatus.CANCELLED, orderLines);
        };
    }
}
