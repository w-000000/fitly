package com.example.minip.recommendation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {
    Optional<RecommendationFeedback> findByRecommendationJobId(Long recommendationJobId);
}
