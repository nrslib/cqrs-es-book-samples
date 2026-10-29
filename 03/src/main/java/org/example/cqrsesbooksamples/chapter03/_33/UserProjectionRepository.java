package org.example.cqrsesbooksamples.chapter03._33;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProjectionRepository extends JpaRepository<UserProjectionEntity, String> {
}
