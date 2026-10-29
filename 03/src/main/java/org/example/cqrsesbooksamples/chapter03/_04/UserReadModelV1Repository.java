package org.example.cqrsesbooksamples.chapter03._04;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReadModelV1Repository extends JpaRepository<UserReadModelV1, String> {
}
