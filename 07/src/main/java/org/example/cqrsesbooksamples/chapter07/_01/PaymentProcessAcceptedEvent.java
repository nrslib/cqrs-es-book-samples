// 紙面照合: 7.1
package org.example.cqrsesbooksamples.chapter07._01;

import java.math.BigDecimal;

import org.example.cqrsesbooksamples.chapter07.support.AccountId;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;


public record PaymentProcessAcceptedEvent(
    PaymentProcessId paymentProcessId,  // プロセスを追跡するためのID
    AccountId accountId,
    BigDecimal amount,
    PaymentMethod paymentMethod
) implements PaymentProcessEvent {}
