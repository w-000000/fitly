package com.example.minip.rental;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GroupRentalRequestRepository extends JpaRepository<GroupRentalRequest,Long>{List<GroupRentalRequest> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);}
