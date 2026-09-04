package com.example.minip.rental;

import com.example.minip.catalog.ProductVariant;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "rental_item", schema = "public")
public class RentalItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_item_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_order_id", nullable = false)
    private RentalOrder order;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "rental_price_snapshot", nullable = false)
    private Long rentalPriceSnapshot;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "converted_to_own_at")
    private Instant convertedToOwnAt;

    @Column(name = "additional_purchase_amount")
    private Long additionalPurchaseAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RentalItem() {
    }

    public RentalItem(RentalOrder order, ProductVariant variant) {
        this.order = order;
        this.variant = variant;
        this.rentalPriceSnapshot = variant.getProduct().getRentalPrice().longValueExact();
        this.status = "RENTED";
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    @JsonIgnore
    public RentalOrder getOrder() {
        return order;
    }

    public String getDatabaseStatus() {
        return status;
    }

    public void requestReturn() {
        if (!"RENTED".equals(status)) {
            throw new IllegalStateException("반납 신청할 수 없는 대여 품목입니다.");
        }
        status = "RETURN_REQUESTED";
        updatedAt = Instant.now();
    }

    public void completeReturn() {
        if (!"RETURN_REQUESTED".equals(status)) {
            throw new IllegalStateException("반납 신청된 품목만 검수할 수 있습니다.");
        }
        status = "RETURNED";
        updatedAt = Instant.now();
    }

    public long purchase() {
        if (!"RENTED".equals(status)) {
            throw new IllegalStateException("대여 중인 상품만 소장할 수 있습니다.");
        }
        long balance = Math.max(
            0L, variant.getProduct().getRetailPrice().longValueExact() - rentalPriceSnapshot
        );
        status = "PURCHASED";
        convertedToOwnAt = Instant.now();
        additionalPurchaseAmount = balance;
        updatedAt = convertedToOwnAt;
        return balance;
    }
}
