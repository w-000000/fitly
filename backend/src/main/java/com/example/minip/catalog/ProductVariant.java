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
    private int availableStock;

    protected ProductVariant() {}
    public ProductVariant(Product product, String sizeName, int availableStock) {
        this.product = product;
        this.sizeName = sizeName;
        this.availableStock = availableStock;
    }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getSizeName() { return sizeName; }
    public int getAvailableStock() { return availableStock; }
    public void changeStock(int delta) {
        if (availableStock + delta < 0) throw new IllegalStateException("대여 가능한 재고가 부족합니다.");
        availableStock += delta;
    }
}
