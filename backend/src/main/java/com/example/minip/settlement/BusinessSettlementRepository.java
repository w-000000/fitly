package com.example.minip.settlement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessSettlementRepository extends JpaRepository<BusinessSettlement, Long> {
    List<BusinessSettlement> findAllByPartnerIdOrderByCreatedAtDesc(Long partnerId);
}
