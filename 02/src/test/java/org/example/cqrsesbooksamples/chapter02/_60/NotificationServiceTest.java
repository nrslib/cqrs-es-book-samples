package org.example.cqrsesbooksamples.chapter02._60;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class NotificationServiceTest {
    @Test
    void 送信完了を観測した時点でリードモデルの有無も確定している() throws Exception {
        var repository = mock(UserReadModelRepository.class);
        var countStarted = new CountDownLatch(1);
        var finishCount = new CountDownLatch(1);
        when(repository.count()).thenAnswer(invocation -> {
            countStarted.countDown();
            assertThat(finishCount.await(5, TimeUnit.SECONDS)).isTrue();
            return 0L;
        });
        var service = new NotificationService(repository);

        try (var executor = Executors.newSingleThreadExecutor()) {
            var send = executor.submit(() -> service.sendWelcomeEmail("john@example.com"));
            try {
                assertThat(countStarted.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(service.sentEmails()).isEmpty();
            } finally {
                finishCount.countDown();
            }
            send.get(5, TimeUnit.SECONDS);
            assertThat(service.sentEmails()).containsExactly("john@example.com");
            assertThat(service.readModelExistedAtSend()).isFalse();
        }
    }
}
