package com.example.minip.recommendation;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"customerId","recommendationJobId","lookKey"}))
public class SavedOutfit {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long customerId;
    private Long recommendationJobId;
    private String lookKey;
    private String title;
    @Column(length=1000) private String description;
    private Instant savedAt;
    protected SavedOutfit(){}
    public SavedOutfit(Long customerId,Long jobId,String lookKey,String title,String description){this.customerId=customerId;this.recommendationJobId=jobId;this.lookKey=lookKey;this.title=title;this.description=description;this.savedAt=Instant.now();}
    public Long getId(){return id;} public Long getCustomerId(){return customerId;} public Long getRecommendationJobId(){return recommendationJobId;}
    public String getLookKey(){return lookKey;} public String getTitle(){return title;} public String getDescription(){return description;} public Instant getSavedAt(){return savedAt;}
}
