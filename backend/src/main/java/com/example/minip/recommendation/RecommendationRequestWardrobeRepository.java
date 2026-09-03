package com.example.minip.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRequestWardrobeRepository
    extends JpaRepository<RecommendationRequestWardrobe, RecommendationRequestWardrobeId> {
}
