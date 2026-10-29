// 検証補助: 2.56
package org.example.cqrsesbooksamples.chapter02._71;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserProjectionTest {
    @Test
    void 誤り版は氏名が名姓の順で連結される() {
        var repository = mock(UserReadModelRepository.class);
        var projection = new UserProjection(repository);

        projection.on(new UserRegisteredEvent("user-123", "Taro", "Yamada", "taro@example.com"));

        var captor = ArgumentCaptor.forClass(UserReadModel.class);
        verify(repository).save(captor.capture());
        // 書籍リスト2.56が示す不具合（名 姓 の順になる）をそのまま観測する
        assertThat(captor.getValue().getFullName()).isEqualTo("Taro Yamada");
    }
}
