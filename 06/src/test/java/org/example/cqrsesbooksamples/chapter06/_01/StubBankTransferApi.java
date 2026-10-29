package org.example.cqrsesbooksamples.chapter06._01;

/**
 * 銀行振込APIのスタブ。
 */
public class StubBankTransferApi implements BankTransferApi {
    private boolean failing = false;

    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    @Override
    public BankTransferResponse requestTransfer(BankTransferRequest request) {
        if (failing) {
            throw new BankApiException("bank api unavailable");
        }
        return new BankTransferResponse("transfer-001");
    }
}
