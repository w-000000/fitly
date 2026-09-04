package com.example.minip.recommendation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleRepository extends JpaRepository<Style, Long> {
    Optional<Style> findByName(String name);
}
