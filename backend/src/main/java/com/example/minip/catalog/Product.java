package com.example.minip.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long partnerId;
    private String name;
    private String category;
    private BigDecimal retailPrice;
    private BigDecimal rentalPrice;
    private String imageUrl;
    private Instant createdAt;

    protected Product() {}

    public Product(Long partnerId, String name, String category, BigDecimal retailPrice, BigDecimal rentalPrice, String imageUrl) {
        this.partnerId = partnerId;
        this.name = name;
        this.category = category;
        this.retailPrice = retailPrice;
        this.rentalPrice = rentalPrice;
        this.imageUrl = imageUrl;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getPartnerId() { return partnerId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getRetailPrice() { return retailPrice; }
    public BigDecimal getRentalPrice() { return rentalPrice; }
    public String getImageUrl() { return imageUrl; }
    public Instant getCreatedAt() { return createdAt; }

    public record CreateRequest(
        @NotNull Long partnerId,
        @NotBlank String name,
        @NotBlank String category,
        @NotNull @Positive BigDecimal retailPrice,
        @NotNull @Positive BigDecimal rentalPrice,
        String imageUrl
    ) {}
}
