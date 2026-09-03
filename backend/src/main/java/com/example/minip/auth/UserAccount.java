package com.example.minip.auth;

import com.example.minip.config.ActorRole;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_account", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String email;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ActorRole role;
    @Column(nullable = false) private Instant createdAt;
    protected UserAccount() {}
    public UserAccount(String email, String name, ActorRole role) {
        this.email = email.toLowerCase(); this.name = name;
        this.role = role; this.createdAt = Instant.now();
    }
    public Long getId() { return id; } public String getEmail() { return email; }
    public String getName() { return name; }
    public ActorRole getRole() { return role; } public Instant getCreatedAt() { return createdAt; }
}
