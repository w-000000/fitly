package com.example.minip.business;

import com.example.minip.auth.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "business_member", schema = "public")
public class BusinessMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "member_role", nullable = false, length = 20)
    private String memberRole;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected BusinessMember() {
    }

    public BusinessMember(Business business, UserAccount user) {
        this.business = business;
        this.user = user;
        this.memberRole = "OWNER";
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }
}
