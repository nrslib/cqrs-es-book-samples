// 紙面照合: 4.20
package org.example.cqrsesbooksamples.chapter04._20;


// 在庫集約の識別子。製品集約のIDと衝突しないよう接頭辞を付けて区別する
public record InventoryId(String productId) {
    @Override
    public String toString() {
        return "inventory-" + productId;
    }
}
