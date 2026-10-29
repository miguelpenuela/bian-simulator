package com.example.biansimulator.identity.api;

import com.example.biansimulator.identity.application.AuthenticatedUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Contratos de entrada/salida del endpoint de autenticación. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "El usuario es obligatorio") @Size(max = 64) String username,
            @NotBlank(message = "La contraseña es obligatoria") @Size(max = 128) String password) {

        @Override
        public String toString() {
            return "LoginRequest[username=" + username + ", password=***]";
        }
    }

    public record CurrentUserResponse(UUID id, String username, String displayName, List<String> roles,
            List<String> permissions) {

        public static CurrentUserResponse from(AuthenticatedUser user) {
            return new CurrentUserResponse(user.id(), user.getUsername(), user.displayName(),
                    user.roles().stream().map(Enum::name).sorted().toList(),
                    user.permissions().stream().map(Enum::name).sorted().toList());
        }
    }

    public record CsrfTokenResponse(String headerName, String token) {
    }
}
