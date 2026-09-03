package com.example.minip.wardrobe;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, Long> {
    List<WardrobeItem> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<WardrobeItem> findAllByUserIdAndIdIn(Long userId, List<Long> ids);
}
