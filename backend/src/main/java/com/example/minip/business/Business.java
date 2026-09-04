package com.example.minip.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "business", schema = "public")
public class Business {
    @Id
    @Column(name = "business_id")
    private Long id;

    @Column(name = "business_name", nullable = false, length = 200)
    private String name;

    @Column(name = "business_number", nullable = false, unique = true, length = 50)
    private String number;

    @Column(name = "business_type", nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Business() {
    }

    public Business(Long id, String name) {
        this.id = id;
        this.name = name;
        this.number = "FITLY-" + id;
        this.type = "SHOP";
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
