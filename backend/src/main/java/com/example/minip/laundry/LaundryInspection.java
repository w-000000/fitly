package com.example.minip.laundry;

import com.example.minip.rental.RentalOrder;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class LaundryInspection {
    public enum DamageGrade { NONE, LIGHT, HEAVY }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.EAGER, optional = false) private RentalOrder rentalOrder;
    @Enumerated(EnumType.STRING) private DamageGrade damageGrade;
    private String notes;
    private boolean cleaned;
    private Instant inspectedAt;
    protected LaundryInspection() {}
    public LaundryInspection(RentalOrder order, DamageGrade grade, String notes, boolean cleaned) {
        this.rentalOrder = order; this.damageGrade = grade; this.notes = notes; this.cleaned = cleaned; this.inspectedAt = Instant.now();
    }
    public Long getId() { return id; } public RentalOrder getRentalOrder() { return rentalOrder; }
    public DamageGrade getDamageGrade() { return damageGrade; } public String getNotes() { return notes; }
    public boolean isCleaned() { return cleaned; } public Instant getInspectedAt() { return inspectedAt; }
}
