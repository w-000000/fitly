package com.example.minip.settlement;

import com.example.minip.business.BusinessContract;
import com.example.minip.business.BusinessContractRepository;
import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import com.example.minip.rental.RentalOrder;
import com.example.minip.rental.RentalOrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class SettlementController {
    private final BusinessSettlementRepository settlements;
    private final RentalOrderRepository orders;
    private final RoleGuard roles;
    private final BusinessContractRepository contracts;

    public SettlementController(BusinessSettlementRepository settlements, RentalOrderRepository orders,
                                RoleGuard roles, BusinessContractRepository contracts) {
        this.settlements = settlements; this.orders = orders; this.roles = roles; this.contracts = contracts;
    }

    @GetMapping("/enterprise/settlements")
    public List<BusinessSettlement> enterprise(@RequestHeader("X-Actor-Role") String role,
                                               @RequestParam Long partnerId) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        return settlements.findAllByContract_Business_IdOrderByCreatedAtDesc(partnerId);
    }

    @PostMapping("/admin/settlements")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessSettlement create(@RequestHeader("X-Actor-Role") String role,
                                     @Valid @RequestBody CreateRequest request) {
        roles.require(role, ActorRole.ROLE_ADMIN);
        if (request.periodEnd().isBefore(request.periodStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "정산 종료일은 시작일과 같거나 이후여야 합니다.");
        }
        BusinessContract contract = contracts.findFirstByBusiness_IdAndStatusOrderByStartDateDesc(request.partnerId(), "ACTIVE")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "활성 사업자 계약을 찾을 수 없습니다."));
        List<RentalOrder> target = orders.findAllByBusinessIdOrderByCreatedAtDesc(request.partnerId())
            .stream().filter(order -> {
                LocalDate created = order.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
                return !created.isBefore(request.periodStart()) && !created.isAfter(request.periodEnd());
            }).toList();
        BigDecimal gross = target.stream().map(RentalOrder::getRentalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amount = target.stream().map(RentalOrder::getSettlementAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return settlements.save(new BusinessSettlement(contract, request.periodStart(), request.periodEnd(),
            gross, BigDecimal.ZERO, gross.subtract(amount).max(BigDecimal.ZERO), amount, request.status()));
    }

    public record CreateRequest(@NotNull Long partnerId, @NotNull LocalDate periodStart,
                                @NotNull LocalDate periodEnd, @NotNull BusinessSettlement.Status status) {}
}
