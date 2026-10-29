package org.example.cqrsesbooksamples.chapter07._04;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * メタデータ方式の経路をまとめて検証するアプリケーション。
 * コントローラ（_05）が付与した processId を集約（_07）がイベントへ引き継ぎ、
 * 収集プロジェクション（_09）が拾う。CorrelationDataProvider（_08）を含む
 * 実構成で、起点情報のメタデータも保存されることを確認する。
 */
@SpringBootApplication(scanBasePackageClasses = {
        org.example.cqrsesbooksamples.chapter07.support.AccountQueryHandler.class,
        org.example.cqrsesbooksamples.chapter07._05.PaymentsController.class,
        org.example.cqrsesbooksamples.chapter07._07.PaymentProcessAggregateAdapter.class,
        org.example.cqrsesbooksamples.chapter07._08.AxonConfiguration.class,
        org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTrackingProjection.class
})
@EntityScan(basePackageClasses = {
        org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTracker.class,
        org.axonframework.eventhandling.tokenstore.jpa.TokenEntry.class,
        org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry.class
})
@EnableJpaRepositories(basePackageClasses =
        org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTrackerRepository.class)
public class Chapter07_04TestApplication {
}
