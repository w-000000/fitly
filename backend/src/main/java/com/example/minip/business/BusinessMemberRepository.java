package com.example.minip.business;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {
    Optional<BusinessMember> findByBusinessIdAndUserId(Long businessId, Long userId);
}
