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
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "recommendation", schema = "public")
public class RecommendationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private RecommendationJob request;

    @Column(nullable = false)
    private int rank;

    @Column(name = "matching_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal matchingScore;

    @Column(name = "styling_comment", nullable = false, columnDefinition = "text")
    private String stylingComment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RecommendationResult() {
    }

    public RecommendationResult(RecommendationJob request, String stylingComment) {
        this(request, 1, BigDecimal.ONE, stylingComment);
    }

    public RecommendationResult(RecommendationJob request, int rank,
                                BigDecimal matchingScore, String stylingComment) {
        this.request = request;
        this.rank = rank;
        this.matchingScore = matchingScore;
        this.stylingComment = stylingComment;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public RecommendationJob getRequest() {
        return request;
    }

    public String getStylingComment() {
        return stylingComment;
    }

    public int getRank() {
        return rank;
    }

    public BigDecimal getMatchingScore() {
        return matchingScore;
    }
}
