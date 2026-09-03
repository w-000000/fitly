package com.example.minip.business;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessContractRepository extends JpaRepository<BusinessContract, Long> {
    Optional<BusinessContract> findFirstByBusinessIdAndStatusOrderByStartDateDesc(Long businessId, String status);
}
