package org.example.cqrsesbooksamples.chapter06._01;

/**
 * クレジット決済事業者APIのスタブ。応答を切り替えてテストに使う。
 */
public class StubCreditApi implements CreditApi {
    private CreditMakePaymentResponse nextResponse = new CreditMakePaymentResponse(200, true);

    public void respondWith(CreditMakePaymentResponse response) {
        this.nextResponse = response;
    }

    @Override
    public CreditMakePaymentResponse makePayment(CreditMakePaymentRequest request) {
        return nextResponse;
    }
}
