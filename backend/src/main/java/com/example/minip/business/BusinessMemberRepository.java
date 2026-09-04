package com.example.minip.business;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {
    @Query("""
        select bm from BusinessMember bm
        where bm.business.id = :businessId and bm.user.id = :userId
        """)
    Optional<BusinessMember> findByBusinessIdAndUserId(
        @Param("businessId") Long businessId,
        @Param("userId") Long userId
    );

    @Query("""
        select bm from BusinessMember bm
        where bm.id = :id
          and bm.business.id = :businessId
          and bm.user.id = :userId
          and bm.status = :status
        """)
    Optional<BusinessMember> findByIdAndBusinessIdAndUserIdAndStatus(
        @Param("id") Long id,
        @Param("businessId") Long businessId,
        @Param("userId") Long userId,
        @Param("status") String status
    );
}
