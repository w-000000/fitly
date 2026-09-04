package com.example.minip.laundry;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface LaundryInspectionRepository extends JpaRepository<LaundryInspection, Long> {
    @Query("select inspection from LaundryInspection inspection where inspection.rentalItem.id in :rentalItemIds")
    List<LaundryInspection> findAllByRentalItemIdIn(@Param("rentalItemIds") Collection<Long> rentalItemIds);
}
