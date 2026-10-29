package org.example.cqrsesbooksamples.chapter03._10;

public class ProductAddedEvent {
    private final String productId;

    public ProductAddedEvent(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
