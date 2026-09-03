package com.example.minip.recommendation;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class RecommendationJob {
    public enum Status { COMPLETED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long customerId;
    private String tpo;
    private String preferredStyle;
    @Column(length = 2000) private String wardrobeDescription;
    private String wardrobeImageUrl;
    @Enumerated(EnumType.STRING) private Status status;
    @Column(length = 1000) private String stylingComment;
    private Instant createdAt;
    protected RecommendationJob() {}
    public RecommendationJob(Long customerId, String tpo, String preferredStyle, String wardrobeDescription, String wardrobeImageUrl) {
        this.customerId = customerId; this.tpo = tpo; this.preferredStyle = preferredStyle;
        this.wardrobeDescription = wardrobeDescription; this.wardrobeImageUrl = wardrobeImageUrl;
        this.status = Status.COMPLETED;
        this.stylingComment = tpo + " 상황에 맞춰 " + preferredStyle + " 무드의 균형 잡힌 조합을 추천합니다.";
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; } public Long getCustomerId() { return customerId; }
    public String getTpo() { return tpo; } public String getPreferredStyle() { return preferredStyle; }
    public String getWardrobeDescription() { return wardrobeDescription; } public String getWardrobeImageUrl() { return wardrobeImageUrl; }
    public Status getStatus() { return status; }
    public String getStylingComment() { return stylingComment; } public Instant getCreatedAt() { return createdAt; }
}
