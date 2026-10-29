package org.example.cqrsesbooksamples.chapter02._17;

record OrderPlacedEvent(String orderId, int amount) {
}

record OrderItemAddedEvent(String orderId, String itemId) {
}

record OrderConfirmedEvent(String orderId) {
}

record OrderShippedEvent(String orderId) {
}
