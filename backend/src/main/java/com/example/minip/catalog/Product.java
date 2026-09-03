package com.example.minip.catalog;

import com.example.minip.recommendation.RentalPurpose;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long partnerId;
    @Enumerated(EnumType.STRING) private RentalPurpose purpose;
    private String category;
    private String name;
    private String description;
    private BigDecimal rentalPrice;
    private BigDecimal retailPrice;
    private BigDecimal settlementRate;
    private String recommendationReason;
    private int stock;
    private String imageUrl;
    private Instant createdAt;

    protected Product() {}
    public Product(Long partnerId, String name, String category, BigDecimal retailPrice, BigDecimal rentalPrice,
                   BigDecimal settlementRate, String imageUrl) {
        this.partnerId = partnerId; this.name = name; this.category = category; this.retailPrice = retailPrice;
        this.rentalPrice = rentalPrice; this.settlementRate = settlementRate; this.imageUrl = imageUrl; this.createdAt = Instant.now();
    }
    public Product(RentalPurpose purpose, String category, String name, String description,
                   int rentalPrice, int purchasePrice, String recommendationReason, int stock) {
        this.partnerId = 0L; this.purpose = purpose; this.category = category; this.name = name;
        this.description = description; this.rentalPrice = BigDecimal.valueOf(rentalPrice);
        this.retailPrice = BigDecimal.valueOf(purchasePrice); this.recommendationReason = recommendationReason;
        this.stock = stock; this.createdAt = Instant.now();
    }
    public Long getId() { return id; } public Long getPartnerId() { return partnerId; }
    public RentalPurpose getPurpose() { return purpose; } public String getCategory() { return category; }
    public String getName() { return name; } public String getDescription() { return description; }
    public BigDecimal getRentalPrice() { return rentalPrice; } public BigDecimal getRetailPrice() { return retailPrice; }
    public BigDecimal getSettlementRate() { return settlementRate; }
    public int getPurchasePrice() { return retailPrice == null ? 0 : retailPrice.intValue(); }
    public String getRecommendationReason() { return recommendationReason; } public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; } public Instant getCreatedAt() { return createdAt; }

    public record CreateRequest(@NotNull Long partnerId, @NotBlank String name, @NotBlank String category,
                                @NotNull @Positive BigDecimal retailPrice, @NotNull @Positive BigDecimal rentalPrice,
                                @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal settlementRate,
                                String imageUrl) {}
}
