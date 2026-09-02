package com.example.minip.catalog;

import com.example.minip.recommendation.RentalPurpose;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPurposeAndRentalPriceLessThanEqualAndStockGreaterThanOrderByRentalPriceAsc(
        RentalPurpose purpose, int budget, int stock
    );
}
