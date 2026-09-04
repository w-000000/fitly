package com.example.minip.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "product_variant", schema = "public")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 30)
    private String size;

    @Column(length = 100)
    private String sku;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductVariant() {
    }

    public ProductVariant(Product product, String size, int availableStock) {
        this.product = product;
        this.size = size;
        this.totalStock = availableStock;
        this.availableStock = availableStock;
        this.status = availableStock == 0 ? "OUT_OF_STOCK" : "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getSizeName() {
        return size;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public String getStatus() {
        return status;
    }

    public void adjustInventory(int delta) {
        if (totalStock + delta < 0 || availableStock + delta < 0) {
            throw new IllegalStateException("재고를 0개 미만으로 변경할 수 없습니다.");
        }
        totalStock += delta;
        availableStock += delta;
        refreshStatus();
        updatedAt = Instant.now();
    }

    public void reserve(int quantity) {
        changeAvailable(-quantity);
    }

    public void release(int quantity) {
        changeAvailable(quantity);
    }

    private void changeAvailable(int delta) {
        if (availableStock + delta < 0) {
            throw new IllegalStateException("대여 가능한 재고가 부족합니다.");
        }
        if (availableStock + delta > totalStock) {
            throw new IllegalStateException("가용 재고가 총재고를 초과할 수 없습니다.");
        }
        availableStock += delta;
        refreshStatus();
        updatedAt = Instant.now();
    }

    private void refreshStatus() {
        status = availableStock == 0 ? "OUT_OF_STOCK" : "ACTIVE";
    }
}
