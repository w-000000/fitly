package com.example.minip.catalog;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findAllByProductId(Long productId);

    @Query("""
        select variant from ProductVariant variant
        join fetch variant.product product
        where product.status = 'ACTIVE'
          and variant.status = 'ACTIVE'
          and variant.availableStock > 0
          and upper(variant.size) = upper(:size)
          and (:budget is null or product.rentalPriceAmount <= :budget)
        order by product.rentalPriceAmount asc, variant.availableStock desc, variant.id asc
        """)
    List<ProductVariant> findRecommendationCandidates(@Param("size") String size,
                                                      @Param("budget") Long budget,
                                                      Pageable pageable);
}
