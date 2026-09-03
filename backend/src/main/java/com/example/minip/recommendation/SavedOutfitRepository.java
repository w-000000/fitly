package com.example.minip.recommendation;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SavedOutfitRepository extends JpaRepository<SavedOutfit,Long>{
 List<SavedOutfit> findAllByCustomerIdOrderBySavedAtDesc(Long customerId);
 Optional<SavedOutfit> findByCustomerIdAndRecommendationJobIdAndLookKey(Long customerId,Long jobId,String lookKey);
}
