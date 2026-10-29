package org.example.cqrsesbooksamples.chapter06;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 非同期の決済フロー（_01）に同期返却の入口（_24）を被せた検証用アプリケーション。
 * スタブAPIのBeanは _01 のテストアプリケーションがスキャン経由で提供する。
 */
@SpringBootApplication(scanBasePackageClasses = {
        org.example.cqrsesbooksamples.chapter06._01.PaymentsController.class,
        org.example.cqrsesbooksamples.chapter06._24.PaymentController.class
})
public class SyncPaymentTestApplication {
}
