package org.example.cqrsesbooksamples.chapter04._20;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInventoryViewRepository extends JpaRepository<ProductInventoryView, String> {
}
