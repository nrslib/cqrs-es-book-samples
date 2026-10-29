// 検証補助: 3.50
package org.example.cqrsesbooksamples.chapter03._67;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;

/**
 * べき等性キー付きの外部API呼び出し（リスト3.49）の検証。
 * 再配信でハンドラが2回動いても、同じべき等性キー（注文ID）で
 * 呼び出されるため、外部API側で重複を排除できる形になっていることを確かめる。
 */
class OrderNotificationHandlerTest {

    @Test
    void 再配信でも同じべき等性キーで外部APIが呼ばれる() throws Exception {
        var apiClient = mock(ExternalApiClient.class);
        var handler = new OrderNotificationHandler(apiClient);
        var handlerAdapter = new AnnotationEventHandlerAdapter(handler);
        var event = GenericEventMessage.asEventMessage(new OrderPlacedEvent(
                "order-1", "john@example.com", new BigDecimal("1000")));

        handlerAdapter.handle(event);
        handlerAdapter.handle(event);

        // 2回とも注文IDがべき等性キーとして渡る
        verify(apiClient, times(2)).notifyOrder(
                "order-1", "john@example.com", new BigDecimal("1000"));
    }
}
