package com.example.minip.rental;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "group_rental_detail", schema = "public")
public class GroupRentalRequest {
    public enum Status {
        RECEIVED, REVIEWING, CONFIRMED, CANCELLED
    }

    @Id
    @Column(name = "rental_order_id")
    private Long id;

    @JsonIgnore
    @MapsId
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rental_order_id")
    private RentalOrder order;

    @Column(nullable = false, length = 100)
    private String purpose;

    @Column(name = "participant_count", nullable = false)
    private int headcount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected GroupRentalRequest() {
    }

    public GroupRentalRequest(RentalOrder order, String purpose, int headcount) {
        this.order = order;
        this.purpose = purpose;
        this.headcount = headcount;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return order.getCustomerId();
    }

    public String getPurpose() {
        return purpose;
    }

    public LocalDate getStartDate() {
        return order.getStartDate();
    }

    public LocalDate getEndDate() {
        return order.getEndDate();
    }

    public int getHeadcount() {
        return headcount;
    }

    public String getRequestedItems() {
        return summaryPart(2);
    }

    public String getContactName() {
        return summaryPart(0);
    }

    public String getContactPhone() {
        return summaryPart(1);
    }

    public Status getStatus() {
        return Status.RECEIVED;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private String summaryPart(int index) {
        String[] parts = order.getShippingAddress().split("\\n", 3);
        return parts.length > index ? parts[index] : "";
    }
}
