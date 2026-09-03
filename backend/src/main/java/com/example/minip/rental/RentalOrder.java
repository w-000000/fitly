package com.example.minip.rental;

import com.example.minip.catalog.ProductVariant;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class RentalOrder {
    public enum Status { RENTED, RETURN_REQUESTED, INSPECTED, OWNED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long customerId;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private ProductVariant variant;
    private int quantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String shippingAddress;
    private BigDecimal rentalAmount;
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) private Status status;
    private Instant createdAt;
    protected RentalOrder() {}
    public RentalOrder(Long customerId, ProductVariant variant, int quantity, LocalDate startDate, LocalDate endDate, String address) {
        this.customerId = customerId; this.variant = variant; this.quantity = quantity; this.startDate = startDate;
        this.endDate = endDate; this.shippingAddress = address;
        this.rentalAmount = variant.getProduct().getRentalPrice().multiply(BigDecimal.valueOf(quantity));
        this.totalAmount = rentalAmount;
        this.status = Status.RENTED; this.createdAt = Instant.now();
    }
    public Long getId() { return id; } public Long getCustomerId() { return customerId; }
    public ProductVariant getVariant() { return variant; } public int getQuantity() { return quantity; }
    public LocalDate getStartDate() { return startDate; } public LocalDate getEndDate() { return endDate; }
    public String getShippingAddress() { return shippingAddress; } public BigDecimal getRentalAmount() { return rentalAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; } public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void requestReturn() { if (status != Status.RENTED) throw new IllegalStateException("반납 신청할 수 없는 주문입니다."); status = Status.RETURN_REQUESTED; }
    public void inspect() { if (status != Status.RETURN_REQUESTED) throw new IllegalStateException("반납 신청된 주문만 검수할 수 있습니다."); status = Status.INSPECTED; }
    public void own() { if (status != Status.RENTED) throw new IllegalStateException("대여 중인 상품만 소장할 수 있습니다."); status = Status.OWNED; }
}
