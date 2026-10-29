package com.example.biansimulator.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class UserAccount {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_code"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserAccount() {
    }

    public UserAccount(UUID id, String username, String displayName, String passwordHash, Set<Role> roles,
            Instant now) {
        this.id = Objects.requireNonNull(id);
        this.username = normalizeUsername(username);
        this.displayName = Objects.requireNonNull(displayName);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.enabled = true;
        this.roles = new HashSet<>(roles);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static String normalizeUsername(String username) {
        return Objects.requireNonNull(username).trim().toLowerCase(Locale.ROOT);
    }

    /** Permisos efectivos: unión de los permisos de todos sus roles. */
    public Set<Permission> effectivePermissions() {
        Set<Permission> result = EnumSet.noneOf(Permission.class);
        roles.forEach(r -> result.addAll(r.getPermissions()));
        return result;
    }

    public void disable(Instant now) {
        this.enabled = false;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }
}
