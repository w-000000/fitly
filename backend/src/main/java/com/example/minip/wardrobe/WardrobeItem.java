package com.example.minip.wardrobe;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class WardrobeItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long customerId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String category;
    private String color;
    private String season;
    @Column(length = 1000) private String description;
    @Column(nullable = false) private String originalFileName;
    @Column(nullable = false) private String imageContentType;
    @JdbcTypeCode(SqlTypes.VARBINARY) @Column(nullable = false, columnDefinition = "bytea") private byte[] imageData;
    @Column(nullable = false) private Instant createdAt;
    private Instant updatedAt;

    protected WardrobeItem() {}
    public WardrobeItem(Long customerId, String name, String category, String color, String season,
                        String description, String originalFileName, String imageContentType, byte[] imageData) {
        this.customerId = customerId; this.name = name; this.category = category; this.color = color;
        this.season = season; this.description = description; this.originalFileName = originalFileName;
        this.imageContentType = imageContentType; this.imageData = imageData; this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    public void update(String name, String category, String color, String season, String description) {
        this.name = name; this.category = category; this.color = color; this.season = season;
        this.description = description; this.updatedAt = Instant.now();
    }
    public Long getId() { return id; } public Long getCustomerId() { return customerId; }
    public String getName() { return name; } public String getCategory() { return category; }
    public String getColor() { return color; } public String getSeason() { return season; }
    public String getDescription() { return description; } public String getOriginalFileName() { return originalFileName; }
    public String getImageContentType() { return imageContentType; } public byte[] getImageData() { return imageData; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
}
