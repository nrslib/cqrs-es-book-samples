// 検証補助: 4.25
package org.example.cqrsesbooksamples.chapter04._29;


public sealed interface OrderEvent permits OrderCreatedEvent, OrderCompletedEvent, OrderCancelledEvent {
    String orderId();
}
