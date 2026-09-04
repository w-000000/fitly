package com.example.minip.rental;

import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/returns")
public class AdminReturnController {
    public enum Action { RECEIVE, COMPLETE_INSPECTION, COMPLETE_LAUNDRY, RESTORE_STOCK }

    private final RentalOrderRepository orders;
    private final RoleGuard roles;

    public AdminReturnController(RentalOrderRepository orders, RoleGuard roles) {
        this.orders = orders;
        this.roles = roles;
    }

    @GetMapping
    public List<RentalOrder> list(@RequestHeader("X-Actor-Role") String role,
                                  @RequestParam(required = false) RentalOrder.ReturnStatus returnStatus,
                                  @RequestParam(required = false) RentalOrder.InspectionStatus inspectionStatus,
                                  @RequestParam(required = false) RentalOrder.LaundryStatus laundryStatus) {
        roles.require(role, ActorRole.ROLE_ADMIN);
        return orders.findAll().stream()
            .filter(order -> order.getStatus() == RentalOrder.Status.RETURN_REQUESTED
                || order.getStatus() == RentalOrder.Status.INSPECTED)
            .filter(order -> returnStatus == null || order.getReturnStatus() == returnStatus)
            .filter(order -> inspectionStatus == null || order.getInspectionStatus() == inspectionStatus)
            .filter(order -> laundryStatus == null || order.getLaundryStatus() == laundryStatus)
            .toList();
    }

    @PatchMapping("/{rentalOrderId}")
    @Transactional
    public RentalOrder update(@RequestHeader("X-Actor-Role") String role,
                              @PathVariable Long rentalOrderId,
                              @Valid @RequestBody UpdateRequest request) {
        roles.require(role, ActorRole.ROLE_ADMIN);
        RentalOrder order = orders.findById(rentalOrderId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "반납 주문을 찾을 수 없습니다."));
        switch (request.action()) {
            case RECEIVE -> order.receiveReturn();
            case COMPLETE_INSPECTION -> order.inspect();
            case COMPLETE_LAUNDRY -> order.completeLaundry();
            case RESTORE_STOCK -> order.restoreStock();
        }
        return order;
    }

    public record UpdateRequest(@NotNull Action action) {}
}
