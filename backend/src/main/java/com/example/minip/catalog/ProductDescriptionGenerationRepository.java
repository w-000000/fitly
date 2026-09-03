package com.example.minip.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDescriptionGenerationRepository
    extends JpaRepository<ProductDescriptionGeneration, Long> {
    Optional<ProductDescriptionGeneration> findFirstByBusinessIdOrderByCreatedAtDesc(Long businessId);
}
