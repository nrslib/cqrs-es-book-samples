package org.example.cqrsesbooksamples.chapter03._65;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class IdempotentUserProjectionTest {
    @Test
    void 同じイベントを2回処理しても結果は変わらない() {
        var store = new InMemoryUserReadModelRepository();
        var projection = new IdempotentUserProjection(store);
        var event = new UserRegisteredEvent("user-1", "John", "john@example.com");

        projection.on(event);
        projection.on(event);  // 重複配信を模す

        assertThat(store.models).hasSize(1);
        assertThat(store.models.get("user-1").getName()).isEqualTo("John");
    }

    private static class InMemoryUserReadModelRepository implements UserReadModelRepository {
        private final Map<String, UserReadModel> models = new HashMap<>();

        @Override
        public Optional<UserReadModel> findById(String userId) {
            return Optional.ofNullable(models.get(userId));
        }

        @Override
        public UserReadModel save(UserReadModel model) {
            models.put(model.getUserId(), model);
            return model;
        }
    }
}
