package com.example.minip.recommendation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, Long> {
    Optional<RecommendationResult> findFirstByRequestIdOrderByRank(Long requestId);
}
