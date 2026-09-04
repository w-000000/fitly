package com.example.minip.rental;

import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    List<RentalOrder> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<RentalOrder> findAllByUserIdAndIdempotencyKeyOrderById(Long userId, String idempotencyKey);

    List<RentalOrder> findAllByOrderGroupKeyOrderById(String orderGroupKey);

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

    @Query("""
        select distinct o from RentalOrder o
        join o.items i
        where i.variant.id = :variantId
          and i.status in :statuses
          and o.startDate <= :endDate
          and o.endDate >= :startDate
        order by o.createdAt desc
        """)
    List<RentalOrder> findAllByVariantIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        @Param("variantId") Long variantId, @Param("statuses") List<String> statuses,
        @Param("endDate") LocalDate endDate, @Param("startDate") LocalDate startDate
    );
}
