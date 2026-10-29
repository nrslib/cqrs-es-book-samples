package org.example.cqrsesbooksamples.chapter02._75;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReadModelRepository extends JpaRepository<UserReadModel, String> {
}
