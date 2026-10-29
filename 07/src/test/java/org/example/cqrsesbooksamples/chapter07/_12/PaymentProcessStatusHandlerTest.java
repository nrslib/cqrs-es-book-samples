// 検証補助: 7.10
package org.example.cqrsesbooksamples.chapter07._12;

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
 * ＠MetaDataValueで特定キーを受け取るイベントハンドラ（リスト7.10）の検証。
 * processId キーの値だけがパラメータとして注入されて追跡に使われること、
 * キーが存在しない場合は required=false の既定により null が渡されることを確かめる。
 */
class PaymentProcessStatusHandlerTest {

    private PaymentProcessAcceptedEvent acceptedEvent() {
        return new PaymentProcessAcceptedEvent(
                new PaymentProcessId(), new AccountId(UUID.randomUUID()),
                new BigDecimal("5000"), PaymentMethod.CREDIT);
    }

    @Test
    void 指定したメタデータキーの値が注入される() throws Exception {
        var handler = new PaymentProcessStatusHandler();
        var handlerAdapter = new AnnotationEventHandlerAdapter(handler);

        handlerAdapter.handle(GenericEventMessage.asEventMessage(acceptedEvent())
                .withMetaData(Map.of("processId", "process-1")));

        assertThat(handler.statuses()).containsEntry("process-1", "ACCEPTED");
    }

    @Test
    void キーが存在しなければnullが渡される() throws Exception {
        var handler = new PaymentProcessStatusHandler();
        var handlerAdapter = new AnnotationEventHandlerAdapter(handler);

        handlerAdapter.handle(GenericEventMessage.asEventMessage(acceptedEvent()));

        assertThat(handler.statuses()).containsEntry(null, "ACCEPTED");
    }
}
