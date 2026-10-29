package org.example.cqrsesbooksamples.chapter03._10;

public interface ProductRepository {
    boolean existsById(String productId);

    ProductReadModel save(ProductReadModel model);
}
