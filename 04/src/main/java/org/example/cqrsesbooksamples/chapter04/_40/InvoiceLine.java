package org.example.cqrsesbooksamples.chapter04._40;

import org.example.cqrsesbooksamples.chapter04._34.Money;

public record InvoiceLine(ProductId productId, int quantity, Money unitPrice) {}
