package com.example.minip.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;

@Entity
@Table(name = "recommendation_request", schema = "public")
public class RecommendationJob {
    public enum Status {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "style_id", nullable = false)
    private Style style;

    @Column(nullable = false, length = 50)
    private String tpo;

    @Column(length = 30)
    private String size;

    @Column(name = "status", nullable = false, length = 20)
    private String databaseStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Transient
    private String wardrobeDescription;

    @Transient
    private String wardrobeImageUrl;

    @Transient
    private String stylingComment;

    protected RecommendationJob() {
    }

    public RecommendationJob(Long userId, String tpo, Style style,
                             String wardrobeDescription, String wardrobeImageUrl) {
        this(userId, tpo, style, null);
        this.wardrobeDescription = wardrobeDescription;
        this.wardrobeImageUrl = wardrobeImageUrl;
    }

    public RecommendationJob(Long userId, String tpo, Style style, String size) {
        this.userId = userId;
        this.tpo = tpo;
        this.style = style;
        this.size = size;
        this.databaseStatus = Status.PENDING.name();
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return userId;
    }

    public String getTpo() {
        return tpo;
    }

    public String getPreferredStyle() {
        return style.getName();
    }

    public String getWardrobeDescription() {
        return wardrobeDescription;
    }

    public String getWardrobeImageUrl() {
        return wardrobeImageUrl;
    }

    public Status getStatus() {
        return Status.valueOf(databaseStatus);
    }

    public String getStylingComment() {
        return stylingComment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStylingComment(String stylingComment) {
        this.stylingComment = stylingComment;
    }

    public void startProcessing() {
        this.databaseStatus = Status.PROCESSING.name();
    }

    public void complete() {
        this.databaseStatus = Status.COMPLETED.name();
        this.completedAt = Instant.now();
    }

    public void fail() {
        this.databaseStatus = Status.FAILED.name();
        this.completedAt = null;
    }
}
