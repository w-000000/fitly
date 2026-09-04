package com.example.minip.recommendation;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "recommendation_feedback", schema = "public",
    uniqueConstraints = @UniqueConstraint(columnNames = "recommendation_job_id"))
public class RecommendationFeedback {
    public enum Sentiment { LIKE, DISLIKE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;
    @Column(name = "recommendation_job_id", nullable = false) private Long recommendationJobId;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20) private Sentiment sentiment;
    @Column(name = "reason_code", length = 50) private String reasonCode;
    @Column(name = "feedback_text", length = 1000) private String feedbackText;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected RecommendationFeedback() {}

    public RecommendationFeedback(Long recommendationJobId, Long customerId, Sentiment sentiment,
                                  String reasonCode, String feedbackText) {
        this.recommendationJobId = recommendationJobId;
        this.customerId = customerId;
        update(sentiment, reasonCode, feedbackText);
    }

    public void update(Sentiment sentiment, String reasonCode, String feedbackText) {
        this.sentiment = sentiment;
        this.reasonCode = reasonCode;
        this.feedbackText = feedbackText;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getRecommendationJobId() { return recommendationJobId; }
    public Long getCustomerId() { return customerId; }
    public Sentiment getSentiment() { return sentiment; }
    public String getReasonCode() { return reasonCode; }
    public String getFeedbackText() { return feedbackText; }
    public Instant getUpdatedAt() { return updatedAt; }
}
