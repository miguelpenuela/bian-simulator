package com.example.biansimulator.support;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.biansimulator.TestcontainersConfiguration;
import com.example.biansimulator.identity.domain.Role;
import com.example.biansimulator.identity.domain.RoleCode;
import com.example.biansimulator.identity.domain.UserAccount;
import com.example.biansimulator.identity.infrastructure.RoleRepository;
import com.example.biansimulator.identity.infrastructure.UserAccountRepository;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Base de pruebas de integración: contexto completo + PostgreSQL real (Testcontainers). */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    public static final String DEMO_USER = "demo";
    public static final String DEMO_PASSWORD = "test-only-Passw0rd!";

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected UserAccountRepository users;

    @Autowired
    protected RoleRepository roles;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected MockMvc mvc;

    @BeforeEach
    void setUpMockMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    /** Sesión autenticada mediante el flujo real (CSRF + login JSON). */
    protected record LoggedIn(MockHttpSession session, Cookie csrf) {

        public MockHttpServletRequestBuilder apply(MockHttpServletRequestBuilder builder) {
            return builder.session(session).cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue());
        }
    }

    protected Cookie fetchCsrf(MockHttpSession session) throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/auth/csrf").session(session)).andReturn();
        return result.getResponse().getCookie("XSRF-TOKEN");
    }

    protected MvcResult login(String username, String password, String clientIp) throws Exception {
        MockHttpSession session = new MockHttpSession();
        Cookie csrf = fetchCsrf(session);
        return mvc.perform(post("/api/v1/auth/login")
                        .session(session)
                        .cookie(csrf)
                        .header("X-XSRF-TOKEN", csrf.getValue())
                        .with(r -> {
                            r.setRemoteAddr(clientIp);
                            return r;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn();
    }

    protected LoggedIn loginAs(String username, String password) throws Exception {
        MvcResult result = login(username, password, "127.0.0.1");
        if (result.getResponse().getStatus() != 200) {
            throw new IllegalStateException("Login falló: " + result.getResponse().getStatus());
        }
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        return new LoggedIn(session, fetchCsrf(session));
    }

    /** Crea (si no existe) un usuario con los roles dados y devuelve su nombre. */
    protected String ensureUser(String username, String password, RoleCode... roleCodes) {
        if (!users.existsByUsername(username)) {
            Set<Role> assigned = new HashSet<>(roles.findAllById(List.of(roleCodes)));
            users.save(new UserAccount(UUID.randomUUID(), username, username, passwordEncoder.encode(password),
                    assigned, Instant.now()));
        }
        return username;
    }
}
