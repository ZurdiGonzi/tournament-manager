package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "username", length = 60, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "role", length = 20, nullable = false)
    private String role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserEntity() {
    }

    public static UserEntity fromDomain(User user) {
        UserEntity e = new UserEntity();
        e.id = user.id();
        e.username = user.username();
        e.passwordHash = user.passwordHash();
        e.role = user.role();
        e.createdAt = user.createdAt();
        return e;
    }

    public User toDomain() {
        return new User(id, username, passwordHash, role, createdAt);
    }
}
