package org.example.cqrsesbooksamples.chapter03._58;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class UserProjectionErrorTest {
    @Test
    void データベースエラーはログに記録して再スローされる() {
        var repository = mock(UserReadModelRepository.class);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        var projection = new UserProjection(repository);

        assertThatThrownBy(() ->
            projection.on(new UserRegisteredEvent("user-1", "John", "john@example.com")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
