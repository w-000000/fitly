package com.example.minip.rental;

import com.example.minip.catalog.ProductVariant;
import com.example.minip.catalog.ProductVariantRepository;
import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalOrderRepository orders;
    private final ProductVariantRepository variants;
    private final RoleGuard roles;
    public RentalController(RentalOrderRepository orders, ProductVariantRepository variants, RoleGuard roles) {
        this.orders = orders; this.variants = variants; this.roles = roles;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public RentalOrder rent(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CreateRequest request) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        ProductVariant variant = variants.findById(request.variantId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품 옵션을 찾을 수 없습니다."));
        variant.changeStock(-request.quantity());
        return orders.save(new RentalOrder(userId, variant, request.quantity(), request.startDate(), request.shippingAddress(), request.insurance()));
    }
    @GetMapping("/mine")
    public List<RentalOrder> mine(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId) {
        roles.require(role, ActorRole.ROLE_CUSTOMER); return orders.findAllByCustomerIdOrderByCreatedAtDesc(userId);
    }
    @GetMapping
    public List<RentalOrder> all(@RequestHeader("X-Actor-Role") String role) {
        roles.require(role, ActorRole.ROLE_ADMIN); return orders.findAll();
    }
    @GetMapping("/partner/{partnerId}/settlements")
    public SettlementView settlements(@RequestHeader("X-Actor-Role") String role, @PathVariable Long partnerId) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        List<RentalOrder> values = orders.findAllByVariantProductPartnerIdOrderByCreatedAtDesc(partnerId);
        BigDecimal total = values.stream().map(RentalOrder::getPartnerSettlementAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SettlementView(partnerId, new BigDecimal("0.85"), total, values);
    }
    @PostMapping("/{id}/return-request")
    @Transactional
    public RentalOrder requestReturn(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        RentalOrder order = ownOrder(id, userId); order.requestReturn(); return order;
    }
    @PostMapping("/{id}/rent-to-own")
    @Transactional
    public OwnResult own(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        RentalOrder order = ownOrder(id, userId);
        BigDecimal balance = order.getVariant().getProduct().getRetailPrice().multiply(BigDecimal.valueOf(order.getQuantity())).subtract(order.getRentalAmount()).max(BigDecimal.ZERO);
        order.own(); return new OwnResult(order, balance);
    }
    private RentalOrder ownOrder(Long id, Long userId) {
        RentalOrder order = orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대여 주문을 찾을 수 없습니다."));
        if (!order.getCustomerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 주문만 처리할 수 있습니다.");
        return order;
    }
    public record CreateRequest(@NotNull Long variantId, @Min(1) int quantity, @NotNull LocalDate startDate, @NotBlank String shippingAddress, boolean insurance) {}
    public record OwnResult(RentalOrder order, BigDecimal remainingBalance) {}
    public record SettlementView(Long partnerId, BigDecimal settlementRate, BigDecimal amount, List<RentalOrder> orders) {}
}
