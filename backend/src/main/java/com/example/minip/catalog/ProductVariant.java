package com.example.minip.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ProductVariant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "product_id")
    private Product product;
    private String sizeName;
    private int totalStock;
    private int availableStock;

    protected ProductVariant() {}
    public ProductVariant(Product product, String sizeName, int availableStock) {
        this.product = product;
        this.sizeName = sizeName;
        this.totalStock = availableStock;
        this.availableStock = availableStock;
    }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getSizeName() { return sizeName; }
    public int getTotalStock() { return totalStock; }
    public int getAvailableStock() { return availableStock; }
    public void adjustInventory(int delta) {
        if (totalStock + delta < 0 || availableStock + delta < 0) throw new IllegalStateException("재고를 0개 미만으로 변경할 수 없습니다.");
        totalStock += delta;
        availableStock += delta;
    }
    public void reserve(int quantity) {
        changeAvailable(-quantity);
    }
    public void release(int quantity) {
        changeAvailable(quantity);
    }
    private void changeAvailable(int delta) {
        if (availableStock + delta < 0) throw new IllegalStateException("대여 가능한 재고가 부족합니다.");
        if (availableStock + delta > totalStock) throw new IllegalStateException("가용 재고가 총재고를 초과할 수 없습니다.");
        availableStock += delta;
    }
}
