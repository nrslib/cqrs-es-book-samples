package org.example.cqrsesbooksamples.chapter07._17;

public record PaymentResult(String orderId, boolean successful) {
    public boolean isSuccessful() {
        return successful;
    }
}
