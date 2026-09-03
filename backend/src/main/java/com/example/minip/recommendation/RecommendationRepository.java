package com.example.minip.recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RecommendationRepository extends JpaRepository<RecommendationRequest, Long> {}
