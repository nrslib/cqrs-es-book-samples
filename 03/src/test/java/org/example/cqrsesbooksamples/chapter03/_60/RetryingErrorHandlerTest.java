package org.example.cqrsesbooksamples.chapter03._60;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.EventProcessingException;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.junit.jupiter.api.Test;

class RetryingErrorHandlerTest {
    @Test
    void リトライで成功すれば例外は伝播しない() throws Exception {
        var attempts = new AtomicInteger();
        EventMessageHandler handler = new RecordingHandler(() -> {
            if (attempts.incrementAndGet() < 2) {
                throw new IllegalStateException("一時的な障害");
            }
        });

        var errorHandler = newErrorHandler(3, 1);
        errorHandler.onError(new IllegalStateException("初回失敗"),
            GenericEventMessage.asEventMessage("payload"), handler);

        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void リトライ上限を超えるとEventProcessingExceptionが送出される() throws Exception {
        EventMessageHandler handler = new RecordingHandler(() -> {
            throw new IllegalStateException("恒久的な障害");
        });

        var errorHandler = newErrorHandler(3, 1);
        assertThatThrownBy(() -> errorHandler.onError(new IllegalStateException("初回失敗"),
                GenericEventMessage.asEventMessage("payload"), handler))
            .isInstanceOf(EventProcessingException.class);
    }

    private ListenerInvocationErrorHandler newErrorHandler(int maxRetries, long retryInterval)
            throws Exception {
        var type = Class.forName(AxonConfig.class.getName() + "$RetryingErrorHandler");
        var constructor = type.getDeclaredConstructor(int.class, long.class);
        constructor.setAccessible(true);
        return (ListenerInvocationErrorHandler) constructor.newInstance(maxRetries, retryInterval);
    }

    private record RecordingHandler(Runnable behavior) implements EventMessageHandler {
        @Override
        public Object handle(EventMessage<?> event) {
            behavior.run();
            return null;
        }

        @Override
        public boolean canHandle(EventMessage<?> event) {
            return true;
        }

        @Override
        public Class<?> getTargetType() {
            return Object.class;
        }
    }
}
