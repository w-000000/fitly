package com.example.minip.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AuthSession {
    @Id private UUID token;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private UserAccount user;
    @Column(nullable = false) private Instant expiresAt;
    protected AuthSession() {}
    public AuthSession(UserAccount user) { this.token = UUID.randomUUID(); this.user = user; this.expiresAt = Instant.now().plusSeconds(86400); }
    public UUID getToken() { return token; } public UserAccount getUser() { return user; } public Instant getExpiresAt() { return expiresAt; }
}
