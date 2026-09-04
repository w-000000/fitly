package com.example.minip.auth;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_role", schema = "public")
public class UserRole {
    @EmbeddedId
    private UserRoleId id;

    protected UserRole() {
    }

    public UserRole(Long userId, Long roleId) {
        this.id = new UserRoleId(userId, roleId);
    }
}
