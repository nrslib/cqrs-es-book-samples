// 紙面照合: 3.6
package org.example.cqrsesbooksamples.chapter03._10;

import org.axonframework.eventhandling.EventHandler;

public class ProductProjection {
    private final ProductRepository productRepository;

    public ProductProjection(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @EventHandler
    public void on(ProductAddedEvent event) {
        // 既存のエントリがあるか確認してから追加操作を行う
        if (!productRepository.existsById(event.getProductId())) {
            productRepository.save(new ProductReadModel(event.getProductId()));
        }
    }
}
