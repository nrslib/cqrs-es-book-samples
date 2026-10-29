package org.example.cqrsesbooksamples.chapter04._27;

public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException(String productId) {
        super("Insufficient inventory for product: " + productId);
    }
}
