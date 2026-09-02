package com.example.minip.catalog;

import com.example.minip.recommendation.RentalPurpose;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RentalPurpose purpose;
    private String category;
    private String name;
    private String description;
    private int rentalPrice;
    private int purchasePrice;
    private String recommendationReason;
    private int stock;

    protected Product() {}

    public Product(RentalPurpose purpose, String category, String name, String description,
                   int rentalPrice, int purchasePrice, String recommendationReason, int stock) {
        this.purpose = purpose;
        this.category = category;
        this.name = name;
        this.description = description;
        this.rentalPrice = rentalPrice;
        this.purchasePrice = purchasePrice;
        this.recommendationReason = recommendationReason;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public RentalPurpose getPurpose() { return purpose; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getRentalPrice() { return rentalPrice; }
    public int getPurchasePrice() { return purchasePrice; }
    public String getRecommendationReason() { return recommendationReason; }
    public int getStock() { return stock; }
}
