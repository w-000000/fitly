package com.example.minip.rental;

import com.example.minip.catalog.ProductVariant;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class RentalOrder {
    public enum Status { RENTED, RETURN_REQUESTED, INSPECTED, OWNED }
    public enum ReturnStatus { NONE, REQUESTED, RECEIVED }
    public enum InspectionStatus { NONE, PENDING, COMPLETED }
    public enum LaundryStatus { NONE, PENDING, COMPLETED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long customerId;
    private Long sourceRecommendationId;
    private String idempotencyKey;
    private String orderGroupKey;
    private boolean multiItemOrder;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private ProductVariant variant;
    private int quantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String shippingAddress;
    private BigDecimal rentalAmount;
    private BigDecimal totalAmount;
    private BigDecimal settlementRate;
    private BigDecimal settlementAmount;
    @Enumerated(EnumType.STRING) private Status status;
    @Enumerated(EnumType.STRING) private ReturnStatus returnStatus;
    @Enumerated(EnumType.STRING) private InspectionStatus inspectionStatus;
    @Enumerated(EnumType.STRING) private LaundryStatus laundryStatus;
    private boolean stockRestored;
    private Instant createdAt;
    protected RentalOrder() {}
    public RentalOrder(Long customerId, ProductVariant variant, int quantity, LocalDate startDate, LocalDate endDate, String address) {
        this(customerId, null, null, null, false, variant, quantity, startDate, endDate, address);
    }
    public RentalOrder(Long customerId, Long sourceRecommendationId, String idempotencyKey, String orderGroupKey,
                       boolean multiItemOrder, ProductVariant variant, int quantity, LocalDate startDate,
                       LocalDate endDate, String address) {
        this.customerId = customerId; this.variant = variant; this.quantity = quantity; this.startDate = startDate;
        this.sourceRecommendationId = sourceRecommendationId; this.idempotencyKey = idempotencyKey;
        this.orderGroupKey = orderGroupKey; this.multiItemOrder = multiItemOrder;
        this.endDate = endDate; this.shippingAddress = address;
        this.rentalAmount = variant.getProduct().getRentalPrice().multiply(BigDecimal.valueOf(quantity));
        this.totalAmount = rentalAmount;
        this.settlementRate = variant.getProduct().getSettlementRate();
        this.settlementAmount = settlementRate == null ? BigDecimal.ZERO : rentalAmount.multiply(settlementRate);
        this.status = Status.RENTED; this.returnStatus = ReturnStatus.NONE;
        this.inspectionStatus = InspectionStatus.NONE; this.laundryStatus = LaundryStatus.NONE;
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; } public Long getCustomerId() { return customerId; }
    public Long getSourceRecommendationId() { return sourceRecommendationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getOrderGroupKey() { return orderGroupKey; }
    public boolean isMultiItemOrder() { return multiItemOrder; }
    public ProductVariant getVariant() { return variant; } public int getQuantity() { return quantity; }
    public LocalDate getStartDate() { return startDate; } public LocalDate getEndDate() { return endDate; }
    public String getShippingAddress() { return shippingAddress; } public BigDecimal getRentalAmount() { return rentalAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; } public Status getStatus() { return status; }
    public BigDecimal getSettlementRate() { return settlementRate; } public BigDecimal getSettlementAmount() { return settlementAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public ReturnStatus getReturnStatus() { return returnStatus == null ? ReturnStatus.NONE : returnStatus; }
    public InspectionStatus getInspectionStatus() { return inspectionStatus == null ? InspectionStatus.NONE : inspectionStatus; }
    public LaundryStatus getLaundryStatus() { return laundryStatus == null ? LaundryStatus.NONE : laundryStatus; }
    public boolean isStockRestored() { return stockRestored; }
    public void requestReturn() { if (status != Status.RENTED) throw new IllegalStateException("반납 신청할 수 없는 주문입니다."); status = Status.RETURN_REQUESTED; returnStatus = ReturnStatus.REQUESTED; inspectionStatus = InspectionStatus.PENDING; }
    public void receiveReturn() { if (status != Status.RETURN_REQUESTED) throw new IllegalStateException("반납 신청된 주문만 접수할 수 있습니다."); returnStatus = ReturnStatus.RECEIVED; }
    public void inspect() { if (status != Status.RETURN_REQUESTED) throw new IllegalStateException("반납 신청된 주문만 검수할 수 있습니다."); if (returnStatus == ReturnStatus.REQUESTED) returnStatus = ReturnStatus.RECEIVED; inspectionStatus = InspectionStatus.COMPLETED; laundryStatus = LaundryStatus.PENDING; status = Status.INSPECTED; }
    public void completeLaundry() { if (inspectionStatus != InspectionStatus.COMPLETED) throw new IllegalStateException("검수 완료 후 세탁할 수 있습니다."); laundryStatus = LaundryStatus.COMPLETED; }
    public void restoreStock() { if (laundryStatus != LaundryStatus.COMPLETED) throw new IllegalStateException("세탁 완료 후 재고를 복구할 수 있습니다."); if (!stockRestored) { variant.release(quantity); stockRestored = true; } }
    public void own() { if (status != Status.RENTED) throw new IllegalStateException("대여 중인 상품만 소장할 수 있습니다."); status = Status.OWNED; }
}
