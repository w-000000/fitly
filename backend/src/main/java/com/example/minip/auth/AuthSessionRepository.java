package com.example.minip.auth;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {}
