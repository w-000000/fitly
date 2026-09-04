package com.example.minip.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "outfit_feedback", schema = "public")
public class SavedOutfit {
    private static final String TEXT_SEPARATOR = "\n---FITLY-DESCRIPTION---\n";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private RecommendationResult recommendation;

    @Column(nullable = false, length = 20)
    private String sentiment;

    @Column(name = "reason_code", length = 50)
    private String lookKey;

    @Column(name = "feedback_text", columnDefinition = "text")
    private String feedbackText;

    @Column(name = "created_at", nullable = false)
    private Instant savedAt;

    protected SavedOutfit() {
    }

    public SavedOutfit(RecommendationResult recommendation, String lookKey,
                       String title, String description) {
        this.recommendation = recommendation;
        this.sentiment = "LIKE";
        this.lookKey = lookKey;
        this.feedbackText = title + TEXT_SEPARATOR + (description == null ? "" : description);
        this.savedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return recommendation.getRequest().getCustomerId();
    }

    public Long getRecommendationJobId() {
        return recommendation.getRequest().getId();
    }

    public String getLookKey() {
        return lookKey;
    }

    public String getTitle() {
        return splitFeedback()[0];
    }

    public String getDescription() {
        return splitFeedback()[1];
    }

    public Instant getSavedAt() {
        return savedAt;
    }

    private String[] splitFeedback() {
        return feedbackText == null ? new String[] {"", ""}
            : feedbackText.split(TEXT_SEPARATOR, 2);
    }
}
