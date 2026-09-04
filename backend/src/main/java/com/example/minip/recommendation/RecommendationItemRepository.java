package com.example.minip.recommendation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {
    List<RecommendationItem> findAllByRecommendationIdOrderBySortOrder(Long recommendationId);
}
