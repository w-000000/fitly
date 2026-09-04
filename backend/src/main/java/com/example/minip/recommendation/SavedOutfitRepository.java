package com.example.minip.recommendation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavedOutfitRepository extends JpaRepository<SavedOutfit, Long> {
    @Query("""
        select feedback from SavedOutfit feedback
        where feedback.recommendation.request.userId = :userId
        order by feedback.savedAt desc
        """)
    List<SavedOutfit> findAllForUser(@Param("userId") Long userId);

    @Query("""
        select feedback from SavedOutfit feedback
        where feedback.recommendation.request.userId = :userId
          and feedback.recommendation.request.id = :requestId
          and feedback.lookKey = :lookKey
        """)
    Optional<SavedOutfit> findSaved(@Param("userId") Long userId,
                                    @Param("requestId") Long requestId,
                                    @Param("lookKey") String lookKey);
}
