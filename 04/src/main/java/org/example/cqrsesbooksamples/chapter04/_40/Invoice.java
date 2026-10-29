// 紙面照合: 4.35
package org.example.cqrsesbooksamples.chapter04._40;

import java.util.ArrayList;
import java.util.List;

import org.example.cqrsesbooksamples.chapter04._34.Money;


public class Invoice {
    private final InvoiceId id;
    private final List<InvoiceLine> invoiceLines;
    private Money totalAmount;

    public Invoice(InvoiceId id, List<InvoiceLine> invoiceLines, Money totalAmount) {
        this.id = id;
        this.invoiceLines = invoiceLines;
        this.totalAmount = totalAmount;
    }

    public InvoiceLineAddedEvent addInvoiceLine(ProductId productId, int quantity, Money unitPrice) {
        var lineTotal = unitPrice.multiply(quantity);
        var newTotal = totalAmount.add(lineTotal);

        // 不変条件: 合計金額は明細行の合計と一致する
        // → 明細追加時に合計を再計算することで保証
        return new InvoiceLineAddedEvent(id, productId, quantity, unitPrice, newTotal);
    }

    public Invoice applyEvent(InvoiceLineAddedEvent event) {
        var newLine = new InvoiceLine(event.productId(), event.quantity(), event.unitPrice());
        var newLines = new ArrayList<>(invoiceLines);
        newLines.add(newLine);
        return new Invoice(id, newLines, event.newTotalAmount());
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public List<InvoiceLine> invoiceLines() {
        return List.copyOf(invoiceLines);
    }
}
