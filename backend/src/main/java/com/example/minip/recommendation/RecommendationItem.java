package com.example.minip.recommendation;

import com.example.minip.catalog.ProductVariant;
import com.example.minip.wardrobe.WardrobeItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_item", schema = "public")
public class RecommendationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private RecommendationResult recommendation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wardrobe_item_id")
    private WardrobeItem wardrobeItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Column(name = "price_snapshot")
    private Long priceSnapshot;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected RecommendationItem() {
    }

    private RecommendationItem(RecommendationResult recommendation, WardrobeItem wardrobeItem,
                               ProductVariant productVariant, String sourceType,
                               Long priceSnapshot, int sortOrder) {
        this.recommendation = recommendation;
        this.wardrobeItem = wardrobeItem;
        this.productVariant = productVariant;
        this.sourceType = sourceType;
        this.priceSnapshot = priceSnapshot;
        this.sortOrder = sortOrder;
    }

    public static RecommendationItem wardrobe(RecommendationResult recommendation,
                                              WardrobeItem item, int sortOrder) {
        return new RecommendationItem(recommendation, item, null, "WARDROBE", null, sortOrder);
    }

    public static RecommendationItem rental(RecommendationResult recommendation,
                                            ProductVariant variant, long priceSnapshot,
                                            int sortOrder) {
        return new RecommendationItem(recommendation, null, variant, "RENTAL_PRODUCT",
            priceSnapshot, sortOrder);
    }

    public Long getId() {
        return id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getPriceSnapshot() {
        return priceSnapshot;
    }

    public WardrobeItem getWardrobeItem() {
        return wardrobeItem;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }
}
