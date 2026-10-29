package org.example.cqrsesbooksamples.chapter06._01;

public class BankTransferResponse {
    private final String transferId;

    public BankTransferResponse(String transferId) {
        this.transferId = transferId;
    }

    public String getTransferId() {
        return transferId;
    }
}
