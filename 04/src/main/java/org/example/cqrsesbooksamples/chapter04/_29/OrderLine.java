// 紙面照合: 4.25
package org.example.cqrsesbooksamples.chapter04._29;


public record OrderLine(String productId, int quantity) {
    public OrderLine {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
