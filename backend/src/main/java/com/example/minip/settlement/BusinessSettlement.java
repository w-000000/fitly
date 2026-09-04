package com.example.minip.settlement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class BusinessSettlement {
    public enum Status { PENDING, SETTLED, FAILED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long partnerId;
    @Column(nullable = false) private LocalDate periodStart;
    @Column(nullable = false) private LocalDate periodEnd;
    @Column(nullable = false) private BigDecimal grossRentalAmount;
    @Column(nullable = false) private BigDecimal settlementAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    @Column(nullable = false) private Instant createdAt;

    protected BusinessSettlement() {}
    public BusinessSettlement(Long partnerId, LocalDate periodStart, LocalDate periodEnd,
                              BigDecimal grossRentalAmount, BigDecimal settlementAmount, Status status) {
        this.partnerId = partnerId; this.periodStart = periodStart; this.periodEnd = periodEnd;
        this.grossRentalAmount = grossRentalAmount; this.settlementAmount = settlementAmount;
        this.status = status; this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public Long getPartnerId() { return partnerId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getGrossRentalAmount() { return grossRentalAmount; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
