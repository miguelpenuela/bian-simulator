package com.example.biansimulator.identity.application;

import com.example.biansimulator.audit.application.AuditActions;
import com.example.biansimulator.audit.application.AuditEntry;
import com.example.biansimulator.audit.application.AuditService;
import com.example.biansimulator.audit.domain.AuditOutcome;
import com.example.biansimulator.identity.domain.UserAccount;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/** Caso de uso de inicio de sesión: bloqueo por intentos, autenticación y auditoría. */
@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService attempts;
    private final AuditService audit;

    public LoginService(AuthenticationManager authenticationManager, LoginAttemptService attempts,
            AuditService audit) {
        this.authenticationManager = authenticationManager;
        this.attempts = attempts;
        this.audit = audit;
    }

    public Authentication authenticate(String rawUsername, String password, String clientIp) {
        String username = UserAccount.normalizeUsername(rawUsername);
        if (attempts.isBlocked(username, clientIp)) {
            audit.record(new AuditEntry(null, username, AuditActions.LOGIN, "USER", null, AuditOutcome.DENIED,
                    Map.of("reason", "too_many_attempts"), clientIp));
            throw new LoginBlockedException(attempts.retryAfterSeconds(username, clientIp));
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
            attempts.reset(username, clientIp);
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
            audit.record(new AuditEntry(user.id(), username, AuditActions.LOGIN, "USER", user.id().toString(),
                    AuditOutcome.SUCCESS, Map.of(), clientIp));
            return auth;
        } catch (AuthenticationException ex) {
            attempts.recordFailure(username, clientIp);
            audit.record(new AuditEntry(null, username, AuditActions.LOGIN, "USER", null, AuditOutcome.FAILURE,
                    Map.of("reason", ex.getClass().getSimpleName()), clientIp));
            throw new InvalidCredentialsException();
        }
    }
}
