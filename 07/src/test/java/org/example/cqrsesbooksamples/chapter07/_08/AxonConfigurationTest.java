package org.example.cqrsesbooksamples.chapter07._08;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.junit.jupiter.api.Test;

class AxonConfigurationTest {
    @Test
    void processIdとメッセージの起点情報を後続メッセージへ伝搬する() {
        var message = new GenericCommandMessage<>(
                "command", Map.of("processId", "process-1", "traceId", "trace-root"));
        var provider = new AxonConfiguration().processIdCorrelationDataProvider();

        var correlationData = provider.correlationDataFor(message);

        assertThat(correlationData.get("processId")).isEqualTo("process-1");
        assertThat(correlationData.get("traceId")).isEqualTo("trace-root");
        assertThat(correlationData.get("correlationId")).isEqualTo(message.getIdentifier());
    }
}
