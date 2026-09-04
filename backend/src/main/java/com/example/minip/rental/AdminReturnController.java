package com.example.minip.rental;

import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import com.example.minip.laundry.LaundryInspection;
import com.example.minip.laundry.LaundryInspectionRepository;
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
    private final LaundryInspectionRepository inspections;

    public AdminReturnController(RentalOrderRepository orders, RoleGuard roles,
                                 LaundryInspectionRepository inspections) {
        this.orders = orders;
        this.roles = roles;
        this.inspections = inspections;
    }

    @GetMapping
    public List<RentalOrder> list(@RequestHeader("X-Actor-Role") String role,
                                  @RequestParam(required = false) RentalOrder.Status status) {
        roles.require(role, ActorRole.ROLE_ADMIN);
        return orders.findAll().stream()
            .filter(order -> order.getStatus() == RentalOrder.Status.RETURN_REQUESTED
                || order.getStatus() == RentalOrder.Status.INSPECTED)
            .filter(order -> status == null || order.getStatus() == status)
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
            case RECEIVE -> ensureReturnRequested(order);
            case COMPLETE_INSPECTION -> completeInspection(order);
            case COMPLETE_LAUNDRY -> completeLaundry(order);
            case RESTORE_STOCK -> restoreStock(order);
        }
        return order;
    }

    private void ensureReturnRequested(RentalOrder order) {
        if (order.getStatus() != RentalOrder.Status.RETURN_REQUESTED) {
            throw new IllegalStateException("반납 신청된 주문만 접수할 수 있습니다.");
        }
    }

    private void completeInspection(RentalOrder order) {
        if (order.getStatus() == RentalOrder.Status.RETURN_REQUESTED) {
            order.inspect();
            order.getRentalItems().forEach(item -> inspections.save(
                new LaundryInspection(item, LaundryInspection.DamageGrade.NONE, null, false)));
        } else if (order.getStatus() != RentalOrder.Status.INSPECTED) {
            throw new IllegalStateException("반납 신청된 주문만 검수할 수 있습니다.");
        }
    }

    private void completeLaundry(RentalOrder order) {
        List<LaundryInspection> values = inspections.findAllByRentalItemIdIn(
            order.getRentalItems().stream().map(item -> item.getId()).toList());
        if (values.isEmpty()) {
            throw new IllegalStateException("검수 완료 후 세탁할 수 있습니다.");
        }
        values.forEach(inspection -> {
            inspection.completeLaundry();
            inspections.save(inspection);
        });
    }

    private void restoreStock(RentalOrder order) {
        List<LaundryInspection> values = inspections.findAllByRentalItemIdIn(
            order.getRentalItems().stream().map(item -> item.getId()).toList());
        if (values.isEmpty() || values.stream().anyMatch(inspection -> !inspection.isCleaned())) {
            throw new IllegalStateException("세탁 완료 후 재고를 복구할 수 있습니다.");
        }
        values.forEach(inspection -> {
            if (!inspection.isInventoryRestored()) {
                inspection.getRentalItem().getVariant().release(1);
                inspection.restoreInventory();
                inspections.save(inspection);
            }
        });
    }

    public record UpdateRequest(@NotNull Action action) {}
}
