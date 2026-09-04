package com.example.minip.settlement;

import com.example.minip.business.BusinessContract;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "business_settlement", schema = "public")
public class BusinessSettlement {
    public enum Status { PENDING, SETTLED, FAILED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private BusinessContract contract;

    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "rental_gross_amount", nullable = false) private Long rentalGrossAmount;
    @Column(name = "rent_to_own_gross_amount", nullable = false) private Long rentToOwnGrossAmount;
    @Column(name = "commission_amount", nullable = false) private Long commissionAmount;
    @Column(name = "settlement_amount", nullable = false) private Long settlementAmount;
    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20) private Status status;
    @Column(name = "settled_at") private Instant settledAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected BusinessSettlement() {}
    public BusinessSettlement(BusinessContract contract, LocalDate periodStart, LocalDate periodEnd,
                              BigDecimal rentalGrossAmount, BigDecimal rentToOwnGrossAmount,
                              BigDecimal commissionAmount, BigDecimal settlementAmount, Status status) {
        this.contract = contract;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.rentalGrossAmount = rentalGrossAmount.longValueExact();
        this.rentToOwnGrossAmount = rentToOwnGrossAmount.longValueExact();
        this.commissionAmount = commissionAmount.longValueExact();
        this.settlementAmount = settlementAmount.longValueExact();
        this.status = status;
        this.settledAt = status == Status.SETTLED ? Instant.now() : null;
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public Long getPartnerId() { return contract.getBusinessId(); }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getGrossRentalAmount() { return BigDecimal.valueOf(rentalGrossAmount); }
    public BigDecimal getRentToOwnGrossAmount() { return BigDecimal.valueOf(rentToOwnGrossAmount); }
    public BigDecimal getCommissionAmount() { return BigDecimal.valueOf(commissionAmount); }
    public BigDecimal getSettlementAmount() { return BigDecimal.valueOf(settlementAmount); }
    public Status getStatus() { return status; }
    public Instant getSettledAt() { return settledAt; }
    public Instant getCreatedAt() { return createdAt; }
}
