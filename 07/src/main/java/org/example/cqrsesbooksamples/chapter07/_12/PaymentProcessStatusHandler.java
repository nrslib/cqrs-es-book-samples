// 紙面照合: 7.10
package org.example.cqrsesbooksamples.chapter07._12;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.annotation.MetaDataValue;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.springframework.stereotype.Component;

// 特定のメタデータキーだけを受け取るイベントハンドラ
@Component
public class PaymentProcessStatusHandler {
    private final Map<String, String> statuses = new LinkedHashMap<>();


    @EventHandler
    public void on(PaymentProcessAcceptedEvent event, @MetaDataValue("processId") String processId) {
        trackEvent(processId, "ACCEPTED");
    }

    private void trackEvent(String processId, String status) {
        statuses.put(processId, status);
    }

    public Map<String, String> statuses() {
        // required=false の既定では processId が null になりうるため、nullキーを許す形で返す
        return Collections.unmodifiableMap(statuses);
    }
}
