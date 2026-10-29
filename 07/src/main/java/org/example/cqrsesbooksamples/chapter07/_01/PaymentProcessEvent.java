// 紙面照合: 7.1
package org.example.cqrsesbooksamples.chapter07._01;

import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;


public interface PaymentProcessEvent {
    PaymentProcessId paymentProcessId();  // すべてのイベントで必須のID
}
