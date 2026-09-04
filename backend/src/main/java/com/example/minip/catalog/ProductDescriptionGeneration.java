package com.example.minip.catalog;

import com.example.minip.business.Business;
import com.example.minip.business.BusinessMember;
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
@Table(name = "product_description_generation", schema = "public")
public class ProductDescriptionGeneration {
    public enum Status {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "generation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_business_member_id", nullable = false)
    private BusinessMember createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "input_image_url", nullable = false, columnDefinition = "text")
    private String inputImageUrl;

    @Column(name = "generated_description", columnDefinition = "text")
    private String generatedDescription;

    @Column(name = "generation_status", nullable = false, length = 20)
    private String generationStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ProductDescriptionGeneration() {
    }

    public ProductDescriptionGeneration(Business business, BusinessMember createdBy,
                                        Product product, String inputImageUrl) {
        this.business = business;
        this.createdBy = createdBy;
        this.product = product;
        this.inputImageUrl = inputImageUrl;
        this.generationStatus = Status.PENDING.name();
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Status getStatus() {
        return Status.valueOf(generationStatus);
    }

    public String getGeneratedDescription() {
        return generatedDescription;
    }

    public void startProcessing() {
        this.generationStatus = Status.PROCESSING.name();
    }

    public void complete(String description) {
        this.generatedDescription = description;
        this.generationStatus = Status.COMPLETED.name();
    }

    public void fail() {
        this.generationStatus = Status.FAILED.name();
    }
}
