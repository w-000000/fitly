package com.example.minip.rental;

import com.example.minip.catalog.ProductVariant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_order", schema = "public")
public class RentalOrder {
    public enum Status {
        RENTED, RETURN_REQUESTED, INSPECTED, OWNED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_order_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_recommendation_id")
    private Long sourceRecommendationId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "order_group_key", length = 100)
    private String orderGroupKey;

    @Column(name = "multi_item_order", nullable = false)
    private boolean multiItemOrder;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @JsonIgnore
    @Column(name = "status", nullable = false, length = 20)
    private String databaseStatus;

    @JsonIgnore
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "rental_start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "rental_end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "text")
    private String shippingAddress;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmountValue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true,
        fetch = FetchType.EAGER)
    private List<RentalItem> items = new ArrayList<>();

    protected RentalOrder() {
    }

    public RentalOrder(Long userId, ProductVariant variant, int quantity,
                       LocalDate startDate, LocalDate endDate, String address) {
        this(userId, null, null, null, false, variant, quantity, startDate, endDate, address);
    }

    public RentalOrder(Long userId, Long sourceRecommendationId, String idempotencyKey,
                       String orderGroupKey, boolean multiItemOrder, ProductVariant variant,
                       int quantity, LocalDate startDate, LocalDate endDate, String address) {
        this.userId = userId;
        this.sourceRecommendationId = sourceRecommendationId;
        this.idempotencyKey = idempotencyKey;
        this.orderGroupKey = orderGroupKey;
        this.multiItemOrder = multiItemOrder;
        this.orderType = "PERSONAL";
        this.databaseStatus = "RENTING";
        this.paymentStatus = "PAID";
        this.startDate = startDate;
        this.endDate = endDate;
        this.shippingAddress = address;
        this.totalAmountValue = variant.getProduct().getRentalPrice().longValueExact() * quantity;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        for (int index = 0; index < quantity; index++) {
            items.add(new RentalItem(this, variant));
        }
    }

    public RentalOrder(Long userId, LocalDate startDate, LocalDate endDate, String requestSummary) {
        this.userId = userId;
        this.orderType = "GROUP";
        this.databaseStatus = "ORDERED";
        this.paymentStatus = "PENDING";
        this.startDate = startDate;
        this.endDate = endDate;
        this.shippingAddress = requestSummary;
        this.totalAmountValue = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return userId;
    }

    public Long getSourceRecommendationId() {
        return sourceRecommendationId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getOrderGroupKey() {
        return orderGroupKey;
    }

    public boolean isMultiItemOrder() {
        return multiItemOrder;
    }

    public ProductVariant getVariant() {
        return items.isEmpty() ? null : items.getFirst().getVariant();
    }

    public int getQuantity() {
        return items.size();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public BigDecimal getRentalAmount() {
        return BigDecimal.valueOf(totalAmountValue);
    }

    public BigDecimal getTotalAmount() {
        return BigDecimal.valueOf(totalAmountValue);
    }

    public Status getStatus() {
        if ("RETURN_DUE".equals(databaseStatus)) {
            return Status.RETURN_REQUESTED;
        }
        if ("COMPLETED".equals(databaseStatus)) {
            boolean purchased = !items.isEmpty()
                && items.stream().allMatch(item -> "PURCHASED".equals(item.getDatabaseStatus()));
            return purchased ? Status.OWNED : Status.INSPECTED;
        }
        return Status.RENTED;
    }

    public BigDecimal getSettlementRate() {
        ProductVariant variant = getVariant();
        return variant == null ? BigDecimal.ZERO : variant.getProduct().getSettlementRate();
    }

    public BigDecimal getSettlementAmount() {
        return getRentalAmount().multiply(getSettlementRate());
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<RentalItem> getRentalItems() {
        return List.copyOf(items);
    }

    public boolean isGroupOrder() {
        return "GROUP".equals(orderType);
    }

    public void requestReturn() {
        if (getStatus() != Status.RENTED || items.isEmpty()) {
            throw new IllegalStateException("반납 신청할 수 없는 주문입니다.");
        }
        items.forEach(RentalItem::requestReturn);
        databaseStatus = "RETURN_DUE";
        updatedAt = Instant.now();
    }

    public void inspect() {
        if (getStatus() != Status.RETURN_REQUESTED) {
            throw new IllegalStateException("반납 신청된 주문만 검수할 수 있습니다.");
        }
        items.forEach(RentalItem::completeReturn);
        databaseStatus = "COMPLETED";
        updatedAt = Instant.now();
    }

    public BigDecimal own() {
        if (getStatus() != Status.RENTED || items.isEmpty()) {
            throw new IllegalStateException("대여 중인 상품만 소장할 수 있습니다.");
        }
        long balance = items.stream().mapToLong(RentalItem::purchase).sum();
        databaseStatus = "COMPLETED";
        updatedAt = Instant.now();
        return BigDecimal.valueOf(balance);
    }
}
