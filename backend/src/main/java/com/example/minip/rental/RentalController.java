package com.example.minip.rental;

import com.example.minip.catalog.ProductVariant;
import com.example.minip.catalog.ProductVariantRepository;
import com.example.minip.business.ReferenceDataService;
import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
    private final ReferenceDataService references;
    public RentalController(RentalOrderRepository orders, ProductVariantRepository variants, RoleGuard roles,
                            ReferenceDataService references) {
        this.orders = orders; this.variants = variants; this.roles = roles; this.references = references;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Object rent(@RequestHeader("X-Actor-Role") String role,
                       @RequestHeader("X-User-Id") Long userId,
                       @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                       @Valid @RequestBody CreateRequest request) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대여 종료일은 시작일과 같거나 이후여야 합니다.");
        }
        references.ensureUser(userId, ActorRole.ROLE_CUSTOMER);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            List<RentalOrder> previous = orders.findAllByUserIdAndIdempotencyKeyOrderById(userId, idempotencyKey);
            if (!previous.isEmpty()) return response(previous);
        }
        List<RentalItemRequest> requestedItems = request.items() == null || request.items().isEmpty()
            ? legacyItem(request) : request.items();
        String groupKey = UUID.randomUUID().toString();
        boolean multi = requestedItems.size() > 1 || (request.items() != null && !request.items().isEmpty());
        List<RentalOrder> created = requestedItems.stream().map(item -> {
            ProductVariant variant = variants.findById(item.variantId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "상품 옵션을 찾을 수 없습니다."));
            variant.reserve(item.quantity());
            return orders.save(new RentalOrder(userId, request.sourceRecommendationId(), idempotencyKey, groupKey,
                multi, variant, item.quantity(), request.startDate(), request.endDate(), request.shippingAddress()));
        }).toList();
        return response(created);
    }
    @GetMapping("/mine")
    public List<RentalOrder> mine(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId) {
        roles.require(role, ActorRole.ROLE_CUSTOMER); return orders.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
    @GetMapping
    public List<RentalOrder> all(@RequestHeader("X-Actor-Role") String role,
                                 @RequestParam(required=false) RentalOrder.Status status) {
        roles.require(role, ActorRole.ROLE_ADMIN);
        return orders.findAll().stream().filter(order -> status == null || order.getStatus() == status).toList();
    }
    @GetMapping("/{id}")
    public RentalOrder get(@RequestHeader("X-Actor-Role") String role,
                           @RequestHeader(value="X-User-Id",required=false) Long userId,@PathVariable Long id) {
        ActorRole actor=roles.require(role,ActorRole.ROLE_CUSTOMER,ActorRole.ROLE_ADMIN,ActorRole.ROLE_PARTNER);
        RentalOrder order=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"대여 주문을 찾을 수 없습니다."));
        if(actor==ActorRole.ROLE_CUSTOMER && !order.getCustomerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"본인의 주문만 조회할 수 있습니다.");
        return order;
    }
    @GetMapping("/partner/{partnerId}/revenue")
    public RevenueView revenue(@RequestHeader("X-Actor-Role") String role, @PathVariable Long partnerId) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        List<RentalOrder> values = orders.findAllByBusinessIdOrderByCreatedAtDesc(partnerId);
        BigDecimal total = values.stream().map(RentalOrder::getRentalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RevenueView(partnerId, total, values);
    }
    @GetMapping("/partner/{partnerId}/settlements")
    public SettlementView settlements(@RequestHeader("X-Actor-Role") String role, @PathVariable Long partnerId) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        List<RentalOrder> values = orders.findAllByBusinessIdOrderByCreatedAtDesc(partnerId);
        BigDecimal total = values.stream().map(RentalOrder::getSettlementAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SettlementView(partnerId, total, values);
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
        BigDecimal balance = order.own(); return new OwnResult(order, balance);
    }
    private RentalOrder ownOrder(Long id, Long userId) {
        RentalOrder order = orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대여 주문을 찾을 수 없습니다."));
        if (!order.getCustomerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 주문만 처리할 수 있습니다.");
        return order;
    }
    private List<RentalItemRequest> legacyItem(CreateRequest request) {
        if (request.variantId() == null || request.quantity() == null || request.quantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "variantId와 quantity 또는 items가 필요합니다.");
        }
        return List.of(new RentalItemRequest(request.variantId(), request.quantity()));
    }
    private Object response(List<RentalOrder> values) {
        if (values.size() == 1 && !values.get(0).isMultiItemOrder()) return values.get(0);
        BigDecimal total = values.stream().map(RentalOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RentalBatch(values.get(0).getOrderGroupKey(), total, values);
    }
    public record CreateRequest(Long variantId, @Min(1) Integer quantity, Long sourceRecommendationId,
                                List<@Valid RentalItemRequest> items, @NotNull LocalDate startDate,
                                @NotNull LocalDate endDate, @NotBlank String shippingAddress) {}
    public record RentalItemRequest(@NotNull Long variantId, @Min(1) int quantity) {}
    public record RentalBatch(String orderGroupKey, BigDecimal totalAmount, List<RentalOrder> items) {}
    public record OwnResult(RentalOrder order, BigDecimal remainingBalance) {}
    public record RevenueView(Long partnerId, BigDecimal rentalRevenue, List<RentalOrder> orders) {}
    public record SettlementView(Long partnerId, BigDecimal settlementAmount, List<RentalOrder> orders) {}
}
