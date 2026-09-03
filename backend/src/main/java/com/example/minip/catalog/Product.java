package com.example.minip.catalog;

import com.example.minip.recommendation.RentalPurpose;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long partnerId;
    @Enumerated(EnumType.STRING) private RentalPurpose purpose;
    private String category;
    private String brand;
    private String name;
    private String description;
    private BigDecimal rentalPrice;
    private BigDecimal retailPrice;
    private String recommendationReason;
    private int stock;
    private String imageUrl;
    private Instant createdAt;

    protected Product() {}
    public Product(Long partnerId, String name, String brand, String category, String description,
                   BigDecimal retailPrice, BigDecimal rentalPrice, String imageUrl) {
        this.partnerId = partnerId; this.name = name; this.brand = brand; this.category = category;
        this.description = description; this.retailPrice = retailPrice;
        this.rentalPrice = rentalPrice; this.imageUrl = imageUrl; this.createdAt = Instant.now();
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
    public String getBrand() { return brand; }
    public BigDecimal getRentalPrice() { return rentalPrice; } public BigDecimal getRetailPrice() { return retailPrice; }
    public int getPurchasePrice() { return retailPrice == null ? 0 : retailPrice.intValue(); }
    public String getRecommendationReason() { return recommendationReason; } public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; } public Instant getCreatedAt() { return createdAt; }

    public void updateDetails(String name, String brand, String category, String description,
                              BigDecimal retailPrice, BigDecimal rentalPrice, String imageUrl) {
        if (name != null) this.name = name;
        if (brand != null) this.brand = brand;
        if (category != null) this.category = category;
        if (description != null) this.description = description;
        if (retailPrice != null) this.retailPrice = retailPrice;
        if (rentalPrice != null) this.rentalPrice = rentalPrice;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    public record CreateRequest(@NotNull Long partnerId, @NotBlank String name, String brand,
                                @NotBlank String category, String description,
                                @NotNull @Positive BigDecimal retailPrice, @NotNull @Positive BigDecimal rentalPrice,
                                String imageUrl) {}
}
