package org.example.cqrsesbooksamples.chapter08._18;

public class PaymentResult {
    private final String status;
    private final String transactionId;
    private final String errorCode;

    public PaymentResult(String status) {
        this(status, null, null);
    }

    public PaymentResult(String status, String transactionId) {
        this(status, transactionId, null);
    }

    private PaymentResult(String status, String transactionId, String errorCode) {
        this.status = status;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
    }

    public static PaymentResult failure(String errorCode) {
        return new PaymentResult("FAILED", null, errorCode);
    }

    public boolean isSuccessful() {
        return "success".equalsIgnoreCase(status);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
