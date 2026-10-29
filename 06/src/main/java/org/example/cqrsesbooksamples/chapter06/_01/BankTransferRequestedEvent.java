// 紙面照合: 6.17
package org.example.cqrsesbooksamples.chapter06._01;


public record BankTransferRequestedEvent(
    PaymentProcessId paymentProcessId,
    String transferId
) {}
