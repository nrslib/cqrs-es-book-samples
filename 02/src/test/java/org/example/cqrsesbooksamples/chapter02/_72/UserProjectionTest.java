package org.example.cqrsesbooksamples.chapter02._72;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserProjectionTest {
    // 削除済み（旧 2.72）
    @Test
    void 氏名は姓名の順で連結される() {
        var repository = mock(UserReadModelRepository.class);
        var projection = new UserProjection(repository);

        projection.on(new UserRegisteredEvent("user-123", "Taro", "Yamada", "taro@example.com"));

        var captor = ArgumentCaptor.forClass(UserReadModel.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Yamada Taro");
    }
}
