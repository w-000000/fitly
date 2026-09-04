package com.example.minip.business;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessContractRepository extends JpaRepository<BusinessContract, Long> {
    Optional<BusinessContract> findFirstByBusiness_IdAndStatusOrderByStartDateDesc(Long businessId, String status);
}
