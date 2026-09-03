package com.example.minip.rental;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRentalRequestRepository extends JpaRepository<GroupRentalRequest, Long> {
    @Query("""
        select detail from GroupRentalRequest detail
        where detail.order.userId = :userId
        order by detail.createdAt desc
        """)
    List<GroupRentalRequest> findAllForUser(@Param("userId") Long userId);
}
