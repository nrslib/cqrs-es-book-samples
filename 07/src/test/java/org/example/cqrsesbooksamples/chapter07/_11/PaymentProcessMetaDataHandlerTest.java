// 検証補助: 7.9
package org.example.cqrsesbooksamples.chapter07._11;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07.support.AccountId;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.junit.jupiter.api.Test;

/**
 * MetaDataをパラメータで受け取るイベントハンドラ（リスト7.9）の検証。
 * イベントに付与したメタデータ全体がパラメータとして注入され、
 * processId を取り出せることを確かめる。
 */
class PaymentProcessMetaDataHandlerTest {

    @Test
    void メタデータ全体がパラメータとして注入される() throws Exception {
        var handler = new PaymentProcessMetaDataHandler();
        var handlerAdapter = new AnnotationEventHandlerAdapter(handler);
        var event = new PaymentProcessAcceptedEvent(
                new PaymentProcessId(), new AccountId(UUID.randomUUID()),
                new BigDecimal("5000"), PaymentMethod.CREDIT);

        handlerAdapter.handle(GenericEventMessage.asEventMessage(event)
                .withMetaData(Map.of("processId", "process-1")));

        assertThat(handler.observedProcessIds()).containsExactly("process-1");
    }
}
