// 紙面照合: 7.9
package org.example.cqrsesbooksamples.chapter07._11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.springframework.stereotype.Component;

// メタデータ全体をパラメータで受け取るイベントハンドラ
@Component
public class PaymentProcessMetaDataHandler {
    private final List<String> observedProcessIds = new CopyOnWriteArrayList<>();


    @EventHandler
    public void on(PaymentProcessAcceptedEvent event, MetaData metaData) {
        var processId = metaData.get("processId").toString();
        observedProcessIds.add(processId);
    }

    public List<String> observedProcessIds() {
        return List.copyOf(observedProcessIds);
    }
}
