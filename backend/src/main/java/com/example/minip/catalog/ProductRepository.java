package com.example.minip.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByBusinessId(Long businessId);

    List<Product> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);

    @Query("""
        select p from Product p
        where p.rentalPriceAmount <= :budget and p.status = 'ACTIVE'
        order by p.rentalPriceAmount asc
        """)
    List<Product> findActiveWithinBudget(@Param("budget") Long budget);
}
