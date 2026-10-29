// 紙面照合: 6.9
package org.example.cqrsesbooksamples.chapter06._10;

import java.time.Duration;

import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.example.cqrsesbooksamples.chapter06._01.CreditMakePaymentFailedEvent;

/**
 * 失敗イベントを受けて、スケジューラに次の試行を預けるイベントハンドラ。
 */
public class CreditRetrySchedulingHandler {
    private static final int MAX_RETRY = 5;

    private final EventScheduler eventScheduler;
    private final EventGateway eventGateway;

    public CreditRetrySchedulingHandler(EventScheduler eventScheduler, EventGateway eventGateway) {
        this.eventScheduler = eventScheduler;
        this.eventGateway = eventGateway;
    }


    @DisallowReplay
    @EventHandler
    public void on(CreditMakePaymentFailedEvent event) {
        var count = event.command().count();
        if (count >= MAX_RETRY) {
            // 上限に達したら再試行を打ち切り、運用フローへ引き渡す
            eventGateway.publish(new CreditMakePaymentCriticalErrorOccurredEvent());
            return;
        }

        // 指数バックオフ（5秒, 10秒, 20秒…上限60秒）で次の試行を予約する
        var delay = Math.min(5L << count, 60L);
        var command = event.command().countUp();
        eventScheduler.schedule(
            Duration.ofSeconds(delay),
            new ExternalRetryRequestedEvent(command)
        );
    }
}
