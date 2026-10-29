// 検証補助: 4.21
package org.example.cqrsesbooksamples.chapter04._22;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InventoryTest {

    @Test
    void 作成イベントから初期在庫を再構築できる() {
        var event = Inventory.create("product-1", 10);

        var inventory = Inventory.applyEvent(event);

        assertThat(inventory.productId()).isEqualTo("product-1");
        assertThat(inventory.quantity()).isEqualTo(10);
    }

    @Test
    void 追加イベントを適用すると在庫数を加算できる() {
        var inventory = new Inventory("product-1", 10);

        var updated = inventory.applyEvent(inventory.add(5));

        assertThat(updated.quantity()).isEqualTo(15);
    }
}
