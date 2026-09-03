package com.example.minip.laundry;

import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import com.example.minip.rental.RentalOrder;
import com.example.minip.rental.RentalOrderRepository;
import com.example.minip.rental.RentalItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

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
        LaundryInspection result = null;
        for (RentalItem item : order.getRentalItems()) {
            if (request.cleaned()) {
                item.getVariant().release(1);
            }
            LaundryInspection saved = inspections.save(
                new LaundryInspection(item, request.damageGrade(), request.notes(), request.cleaned())
            );
            if (result == null) {
                result = saved;
            }
        }
        return result;
    }
    public record InspectRequest(@NotNull Long rentalOrderId, @NotNull LaundryInspection.DamageGrade damageGrade, String notes, boolean cleaned) {}
    @GetMapping
    public List<LaundryInspection> list(@RequestHeader("X-Actor-Role") String role) {
        roles.require(role, ActorRole.ROLE_ADMIN); return inspections.findAll();
    }
}
