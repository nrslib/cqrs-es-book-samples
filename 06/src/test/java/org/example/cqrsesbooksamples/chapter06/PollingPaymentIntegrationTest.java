package org.example.cqrsesbooksamples.chapter06;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.example.cqrsesbooksamples.chapter06._01.AccountData;
import org.example.cqrsesbooksamples.chapter06._01.AccountQueryHandler;
import org.example.cqrsesbooksamples.chapter06._01.CreditMakePaymentResponse;
import org.example.cqrsesbooksamples.chapter06._01.PaymentMethod;
import org.example.cqrsesbooksamples.chapter06._01.StubCreditApi;
import org.example.cqrsesbooksamples.chapter06._24.PaymentPollingController;
import org.example.cqrsesbooksamples.chapter06._24.PaymentRequest;
import org.example.cqrsesbooksamples.chapter06._24.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter06_polling;DB_CLOSE_DELAY=-1")
class PollingPaymentIntegrationTest {
    @Autowired
    private PaymentPollingController controller;

    @Autowired
    private AccountQueryHandler accountQueryHandler;

    @Autowired
    private StubCreditApi creditApi;

    @Test
    void ポーリング方式で決済完了が同期的に返る() {
        var accountId = UUID.randomUUID();
        accountQueryHandler.register(new AccountData(accountId, "Taro"));
        creditApi.respondWith(new CreditMakePaymentResponse(200, true));

        var result = controller.requestPaymentSyncPolling(
            new PaymentRequest(accountId, new BigDecimal("5000"), PaymentMethod.CREDIT)).block();

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void ポーリング方式で決済拒否が同期的に返る() {
        var accountId = UUID.randomUUID();
        accountQueryHandler.register(new AccountData(accountId, "Jiro"));
        creditApi.respondWith(new CreditMakePaymentResponse(200, false));

        var result = controller.requestPaymentSyncPolling(
            new PaymentRequest(accountId, new BigDecimal("5000"), PaymentMethod.CREDIT)).block();

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
    }
}
