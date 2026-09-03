package com.example.minip.laundry;

import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import com.example.minip.rental.RentalOrder;
import com.example.minip.rental.RentalOrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/laundry/inspections")
public class LaundryController {
    private final RentalOrderRepository orders;
    private final LaundryInspectionRepository inspections;
    private final RoleGuard roles;
    public LaundryController(RentalOrderRepository orders, LaundryInspectionRepository inspections, RoleGuard roles) {
        this.orders = orders; this.inspections = inspections; this.roles = roles;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public LaundryInspection inspect(@RequestHeader("X-Actor-Role") String role, @Valid @RequestBody InspectRequest request) {
        roles.require(role, ActorRole.ROLE_ADMIN);
        RentalOrder order = orders.findById(request.rentalOrderId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대여 주문을 찾을 수 없습니다."));
        order.inspect();
        if (request.cleaned()) order.getVariant().changeStock(order.getQuantity());
        return inspections.save(new LaundryInspection(order, request.damageGrade(), request.notes(), request.cleaned()));
    }
    public record InspectRequest(@NotNull Long rentalOrderId, @NotNull LaundryInspection.DamageGrade damageGrade, String notes, boolean cleaned) {}
}
