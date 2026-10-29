package org.example.cqrsesbooksamples.chapter06._01;

public interface BankTransferApi {
    BankTransferResponse requestTransfer(BankTransferRequest request) throws BankApiException;
}
