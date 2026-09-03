package com.example.minip.catalog;

import com.example.minip.business.Business;
import com.example.minip.business.BusinessMember;
import com.example.minip.recommendation.RentalPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "product", schema = "public")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_business_member_id", nullable = false)
    private BusinessMember createdBy;

    @Column(name = "product_name", nullable = false, length = 200)
    private String name;

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(name = "original_price", nullable = false)
    private Long originalPrice;

    @Column(name = "rental_price", nullable = false)
    private Long rentalPriceAmount;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    private String imageUrl;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Formula("(select coalesce(sum(v.available_stock), 0) from public.product_variant v where v.product_id = product_id)")
    private int stock;

    @Formula("(select max(bc.commission_rate) from public.business_contract bc where bc.business_id = business_id and bc.status = 'ACTIVE')")
    private BigDecimal settlementRate;

    protected Product() {
    }

    public Product(Business business, BusinessMember createdBy, String name, String brand,
                   String category, String description, BigDecimal retailPrice,
                   BigDecimal rentalPrice, String imageUrl) {
        this.business = business;
        this.createdBy = createdBy;
        this.name = name;
        this.brand = brand == null || brand.isBlank() ? "FITLY" : brand;
        this.category = normalizeCategory(category);
        this.description = description == null || description.isBlank() ? name : description;
        this.originalPrice = retailPrice.longValueExact();
        this.rentalPriceAmount = rentalPrice.longValueExact();
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPartnerId() {
        return business.getId();
    }

    public RentalPurpose getPurpose() {
        return switch (category) {
            case "SHOES", "ACCESSORY" -> RentalPurpose.EVENT;
            default -> RentalPurpose.PERSONAL;
        };
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public BigDecimal getRentalPrice() {
        return BigDecimal.valueOf(rentalPriceAmount);
    }

    public BigDecimal getRetailPrice() {
        return BigDecimal.valueOf(originalPrice);
    }

    public BigDecimal getSettlementRate() {
        return settlementRate == null ? BigDecimal.ZERO : settlementRate;
    }

    public int getPurchasePrice() {
        return Math.toIntExact(originalPrice);
    }

    public String getRecommendationReason() {
        return description;
    }

    public int getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updateDetails(String name, String brand, String category, String description,
                              BigDecimal retailPrice, BigDecimal rentalPrice, String imageUrl) {
        if (name != null) {
            this.name = name;
        }
        if (brand != null) {
            this.brand = brand;
        }
        if (category != null) {
            this.category = normalizeCategory(category);
        }
        if (description != null) {
            this.description = description;
        }
        if (retailPrice != null) {
            this.originalPrice = retailPrice.longValueExact();
        }
        if (rentalPrice != null) {
            this.rentalPriceAmount = rentalPrice.longValueExact();
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        this.updatedAt = Instant.now();
    }

    public static String normalizeCategory(String value) {
        if (value == null) {
            return "ACCESSORY";
        }
        return switch (value.trim().toUpperCase()) {
            case "TOP", "SHIRT" -> "TOP";
            case "BOTTOM", "PANTS", "SKIRT" -> "BOTTOM";
            case "OUTER", "JACKET", "COAT" -> "OUTER";
            case "SHOES", "SHOE" -> "SHOES";
            default -> "ACCESSORY";
        };
    }

    public record CreateRequest(@NotNull Long partnerId, @NotBlank String name, String brand,
                                @NotBlank String category, String description,
                                @NotNull @Positive BigDecimal retailPrice,
                                @NotNull @Positive BigDecimal rentalPrice,
                                @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal settlementRate,
                                String imageUrl) {
    }
}
