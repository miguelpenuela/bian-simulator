package com.example.biansimulator.identity.infrastructure;

import com.example.biansimulator.identity.domain.Role;
import com.example.biansimulator.identity.domain.RoleCode;
import com.example.biansimulator.identity.domain.UserAccount;
import java.time.Clock;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el usuario de demostración en los perfiles dev/demo/test, solo si se definió
 * su contraseña (DEMO_USER_PASSWORD). Nunca hay credenciales en el repositorio.
 */
@Component
@Profile({"dev", "demo", "test"})
public class DemoUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserInitializer.class);

    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String username;
    private final String password;

    public DemoUserInitializer(UserAccountRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
            Clock clock, @Value("${app.demo.user.username:demo}") String username,
            @Value("${app.demo.user.password:}") String password) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.username = UserAccount.normalizeUsername(username);
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (password == null || password.isBlank()) {
            log.warn("DEMO_USER_PASSWORD no definido: no se crea el usuario de demostración.");
            return;
        }
        if (users.existsByUsername(username)) {
            return;
        }
        Set<Role> demoRoles = new HashSet<>(roles.findAllById(Set.of(RoleCode.ARCHITECT, RoleCode.LEARNER)));
        users.save(new UserAccount(UUID.randomUUID(), username, "Usuario de demostración",
                passwordEncoder.encode(password), demoRoles, clock.instant()));
        log.info("Usuario de demostración '{}' creado (roles ARCHITECT y LEARNER).", username);
    }
}
