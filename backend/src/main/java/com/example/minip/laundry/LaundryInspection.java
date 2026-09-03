package com.example.minip.laundry;

import com.example.minip.rental.RentalItem;
import com.example.minip.rental.RentalOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;

@Entity
@Table(name = "return_process", schema = "public")
public class LaundryInspection {
    public enum DamageGrade {
        NONE, LIGHT, HEAVY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_process_id")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rental_item_id", nullable = false)
    private RentalItem rentalItem;

    @Column(name = "processed_by_admin_user_id")
    private Long processedByAdminUserId;

    @Column(name = "return_status", nullable = false, length = 20)
    private String returnStatus;

    @Column(name = "inspection_status", nullable = false, length = 20)
    private String inspectionStatus;

    @Column(name = "inspection_grade", length = 20)
    private String inspectionGrade;

    @Column(name = "laundry_status", nullable = false, length = 20)
    private String laundryStatus;

    @Column(name = "inventory_restored", nullable = false)
    private boolean inventoryRestored;

    @Column(name = "return_requested_at", nullable = false)
    private Instant returnRequestedAt;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @Column(name = "inspected_at")
    private Instant inspectedAt;

    @Column(name = "cleaned_at")
    private Instant cleanedAt;

    @Column(name = "inventory_restored_at")
    private Instant inventoryRestoredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private String notes;

    protected LaundryInspection() {
    }

    public LaundryInspection(RentalItem rentalItem, DamageGrade grade, String notes, boolean cleaned) {
        Instant now = Instant.now();
        this.rentalItem = rentalItem;
        this.returnStatus = "COMPLETED";
        this.inspectionStatus = "COMPLETED";
        this.inspectionGrade = toDatabaseGrade(grade);
        this.laundryStatus = cleaned ? "COMPLETED" : "PENDING";
        this.inventoryRestored = cleaned;
        this.returnRequestedAt = now;
        this.returnedAt = now;
        this.inspectedAt = now;
        this.cleanedAt = cleaned ? now : null;
        this.inventoryRestoredAt = cleaned ? now : null;
        this.createdAt = now;
        this.updatedAt = now;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public RentalOrder getRentalOrder() {
        return rentalItem.getOrder();
    }

    public DamageGrade getDamageGrade() {
        return switch (inspectionGrade) {
            case "MINOR_STAIN" -> DamageGrade.LIGHT;
            case "DAMAGED" -> DamageGrade.HEAVY;
            default -> DamageGrade.NONE;
        };
    }

    public String getNotes() {
        return notes;
    }

    public boolean isCleaned() {
        return "COMPLETED".equals(laundryStatus);
    }

    public Instant getInspectedAt() {
        return inspectedAt;
    }

    private String toDatabaseGrade(DamageGrade grade) {
        return switch (grade) {
            case NONE -> "NORMAL";
            case LIGHT -> "MINOR_STAIN";
            case HEAVY -> "DAMAGED";
        };
    }
}
