package org.example.cqrsesbooksamples.chapter03._63;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Chapter03_63TestApplication {

    /** 旧 3.61のプロジェクションが使うテスト用のインメモリリポジトリ */
    @Component
    static class InMemoryUserReadModelRepository implements UserReadModelRepository {
        private final Map<String, UserReadModel> store = new ConcurrentHashMap<>();

        @Override
        public Optional<UserReadModel> findById(String userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public UserReadModel save(UserReadModel model) {
            store.put(model.getUserId(), model);
            return model;
        }
    }

}
