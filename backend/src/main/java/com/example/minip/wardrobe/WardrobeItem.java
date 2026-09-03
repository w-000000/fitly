package com.example.minip.wardrobe;

import com.example.minip.catalog.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Base64;

@Entity
@Table(name = "wardrobe_item", schema = "public")
public class WardrobeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wardrobe_item_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    private String imageUrl;

    @Column(name = "item_name", nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(name = "ai_detected_category", length = 30)
    private String aiDetectedCategory;

    @Column(name = "ai_detected_color", length = 50)
    private String aiDetectedColor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private String season;

    @Transient
    private String description;

    @Transient
    private String originalFileName;

    protected WardrobeItem() {
    }

    public WardrobeItem(Long userId, String name, String category, String color, String season,
                        String description, String originalFileName, String imageContentType,
                        byte[] imageData) {
        this.userId = userId;
        this.name = name;
        this.category = Product.normalizeCategory(category);
        this.color = color == null || color.isBlank() ? "UNKNOWN" : color;
        this.season = season;
        this.description = description;
        this.originalFileName = originalFileName;
        this.imageUrl = "data:" + imageContentType + ";base64," + Base64.getEncoder().encodeToString(imageData);
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void update(String name, String category, String color, String season, String description) {
        this.name = name;
        this.category = Product.normalizeCategory(category);
        this.color = color == null || color.isBlank() ? "UNKNOWN" : color;
        this.season = season;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return switch (category) {
            case "BOTTOM" -> "PANTS";
            default -> category;
        };
    }

    public String getCanonicalCategory() {
        return category;
    }

    public String getColor() {
        return color;
    }

    public String getAiDetectedCategory() {
        return aiDetectedCategory;
    }

    public String getAiDetectedColor() {
        return aiDetectedColor;
    }

    public String getStoredImageUrl() {
        return imageUrl;
    }

    public String getSeason() {
        return season;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginalFileName() {
        return originalFileName == null ? "wardrobe-image" : originalFileName;
    }

    public String getImageContentType() {
        int delimiter = imageUrl.indexOf(';');
        return imageUrl.startsWith("data:") && delimiter > 5
            ? imageUrl.substring(5, delimiter) : "application/octet-stream";
    }

    public byte[] getImageData() {
        int delimiter = imageUrl.indexOf(',');
        if (!imageUrl.startsWith("data:") || delimiter < 0) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(imageUrl.substring(delimiter + 1));
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
