// 検証補助: 2.59
package org.example.cqrsesbooksamples.chapter02._75;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.axonframework.eventhandling.ReplayStatus;
import org.junit.jupiter.api.Test;

class ReplayStatusTest {

    @Test
    void 通常処理ではリードモデル更新とメール送信の両方が行われる() {
        var repository = mock(UserReadModelRepository.class);
        var notificationService = new NotificationService();
        var projection = new UserProjection(repository, notificationService);

        projection.on(new UserRegisteredEvent("user-1", "John", "john@example.com"), ReplayStatus.REGULAR);

        verify(repository).save(any(UserReadModel.class));
        assertThat(notificationService.sentEmails()).containsExactly("john@example.com");
    }

    @Test
    void リプレイ中はリードモデルだけ更新されメール送信はスキップされる() {
        var repository = mock(UserReadModelRepository.class);
        var notificationService = new NotificationService();
        var projection = new UserProjection(repository, notificationService);

        projection.on(new UserRegisteredEvent("user-1", "John", "john@example.com"), ReplayStatus.REPLAY);

        verify(repository).save(any(UserReadModel.class));
        assertThat(notificationService.sentEmails()).isEmpty();
    }
}
