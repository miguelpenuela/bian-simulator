package com.example.biansimulator.identity.application;

import com.example.biansimulator.identity.domain.Permission;
import com.example.biansimulator.identity.domain.RoleCode;
import com.example.biansimulator.identity.domain.UserAccount;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Principal almacenado en la sesión. Las autoridades son los permisos efectivos
 * más {@code ROLE_<rol>}. El hash de contraseña se borra tras autenticar.
 */
public final class AuthenticatedUser implements UserDetails, CredentialsContainer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String username;
    private final String displayName;
    private String passwordHash;
    private final boolean enabled;
    private final Set<RoleCode> roles;
    private final Set<Permission> permissions;

    public AuthenticatedUser(UUID id, String username, String displayName, String passwordHash, boolean enabled,
            Set<RoleCode> roles, Set<Permission> permissions) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roles = Set.copyOf(roles);
        this.permissions = Set.copyOf(permissions);
    }

    public static AuthenticatedUser from(UserAccount account) {
        return new AuthenticatedUser(account.getId(), account.getUsername(), account.getDisplayName(),
                account.getPasswordHash(), account.isEnabled(),
                account.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet()),
                account.effectivePermissions());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.name())));
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name())));
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }

    public UUID id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Set<RoleCode> roles() {
        return roles;
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}
