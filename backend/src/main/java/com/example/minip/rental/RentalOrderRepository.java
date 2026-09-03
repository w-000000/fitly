package com.example.minip.rental;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    List<RentalOrder> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<RentalOrder> findAllByVariantProductPartnerIdOrderByCreatedAtDesc(Long partnerId);
    List<RentalOrder> findAllByVariantIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Long variantId, List<RentalOrder.Status> statuses, LocalDate endDate, LocalDate startDate
    );
}
