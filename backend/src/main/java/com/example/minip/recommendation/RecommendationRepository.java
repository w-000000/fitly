package com.example.minip.recommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RecommendationRepository extends JpaRepository<RecommendationJob, Long> {
    List<RecommendationJob> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
