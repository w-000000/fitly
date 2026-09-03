package com.example.minip.rental;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    List<RentalOrder> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
        select distinct o from RentalOrder o
        join o.items i
        join i.variant v
        join v.product p
        where p.business.id = :businessId
        order by o.createdAt desc
        """)
    List<RentalOrder> findAllByBusinessIdOrderByCreatedAtDesc(
        @Param("businessId") Long businessId
    );
}
