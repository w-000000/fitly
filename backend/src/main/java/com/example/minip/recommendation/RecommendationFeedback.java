package com.example.minip.recommendation;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "recommendationJobId"))
public class RecommendationFeedback {
    public enum Sentiment { LIKE, DISLIKE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long recommendationJobId;
    @Column(nullable = false) private Long customerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Sentiment sentiment;
    private String reasonCode;
    @Column(length = 1000) private String feedbackText;
    @Column(nullable = false) private Instant updatedAt;

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
