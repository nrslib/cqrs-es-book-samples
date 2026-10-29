// 検証補助: 7.17
package org.example.cqrsesbooksamples.chapter07._20;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * メッセージブローカーを介した契約（リスト7.17）を検証するアプリケーション。
 * 注文システム側の送信（OrderService）と決済システム側の受信
 * （PaymentMessageListener）だけをスキャンし、Kafka の自動構成で接続する。
 */
@SpringBootApplication(scanBasePackageClasses = OrderService.class)
public class Chapter07_20TestApplication {
}
