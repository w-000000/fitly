package com.example.minip.catalog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findAllByProductId(Long productId);
}
