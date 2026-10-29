package org.example.cqrsesbooksamples.chapter03._63;

import java.util.Optional;

public interface UserReadModelRepository {
    Optional<UserReadModel> findById(String userId);

    UserReadModel save(UserReadModel model);
}
