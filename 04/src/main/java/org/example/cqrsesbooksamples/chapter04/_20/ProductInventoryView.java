package org.example.cqrsesbooksamples.chapter04._20;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 削除済み（旧 4.23）
@Entity
@Table(name = "product_inventory_view")
public class ProductInventoryView {
    @Id
    private String productId;
    private String name;
    private BigDecimal price;
    private int quantity;

    protected ProductInventoryView() {}

    public ProductInventoryView(String productId, String name, BigDecimal price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
