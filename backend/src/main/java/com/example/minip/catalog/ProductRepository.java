package com.example.minip.catalog;

import com.example.minip.recommendation.RentalPurpose;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByPartnerId(Long partnerId);
    List<Product> findByPurposeAndRentalPriceLessThanEqualAndStockGreaterThanOrderByRentalPriceAsc(
        RentalPurpose purpose, BigDecimal budget, int stock
    );
}
