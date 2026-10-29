package org.example.cqrsesbooksamples.chapter04._40;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.junit.jupiter.api.Test;

class InvoiceTest {
    @Test
    void 明細追加で合計金額が再計算される() {
        var invoice = new Invoice(new InvoiceId("inv-1"), List.of(), money("0"));

        var event = invoice.addInvoiceLine(new ProductId("p-1"), 3, money("100"));
        assertThat(event.newTotalAmount()).isEqualTo(money("300"));

        invoice = invoice.applyEvent(event);
        assertThat(invoice.totalAmount()).isEqualTo(money("300"));
        assertThat(invoice.invoiceLines()).hasSize(1);

        var second = invoice.addInvoiceLine(new ProductId("p-2"), 1, money("50"));
        invoice = invoice.applyEvent(second);
        assertThat(invoice.totalAmount()).isEqualTo(money("350"));
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount));
    }
}
