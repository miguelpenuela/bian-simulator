package com.example.biansimulator.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.biansimulator.audit.application.AuditActions;
import com.example.biansimulator.audit.domain.AuditEvent;
import com.example.biansimulator.audit.domain.AuditOutcome;
import com.example.biansimulator.audit.infrastructure.AuditEventRepository;
import com.example.biansimulator.support.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

class AuthenticationFlowIT extends AbstractIntegrationTest {

    @Autowired
    AuditEventRepository auditEvents;

    @Test
    void loginMeLogoutRoundTrip() throws Exception {
        MvcResult login = login(DEMO_USER, DEMO_PASSWORD, "127.0.0.1");
        assertThat(login.getResponse().getStatus()).isEqualTo(200);
        assertThat(login.getResponse().getContentAsString())
                .contains("\"username\":\"demo\"")
                .contains("CATALOG_READ")
                .doesNotContain("password");

        LoggedIn session = loginAs(DEMO_USER, DEMO_PASSWORD);

        mvc.perform(get("/api/v1/auth/me").session(session.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo"))
                .andExpect(jsonPath("$.roles[0]").value("ARCHITECT"));

        mvc.perform(session.apply(post("/api/v1/auth/logout")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/auth/me").session(session.session()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("urn:bian-simulator:problem:unauthorized"));

        List<AuditEvent> logouts = auditEvents.findByActorUsernameAndActionOrderByOccurredAtDesc(
                DEMO_USER, AuditActions.LOGOUT);
        assertThat(logouts).isNotEmpty();
    }

    @Test
    void sessionIdChangesOnLogin() throws Exception {
        MockHttpSession anonymous = new MockHttpSession();
        String before = anonymous.getId();
        var csrf = fetchCsrf(anonymous);
        MvcResult result = mvc.perform(post("/api/v1/auth/login").session(anonymous).cookie(csrf)
                        .header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"" + DEMO_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getRequest().getSession(false).getId()).isNotEqualTo(before);
    }

    @Test
    void wrongPasswordIsRejectedAndAudited() throws Exception {
        MvcResult result = login(DEMO_USER, "wrong-password", "10.0.0.1");
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(result.getResponse().getContentAsString())
                .contains("urn:bian-simulator:problem:invalid-credentials");

        assertThat(auditEvents.findByActorUsernameAndActionOrderByOccurredAtDesc(DEMO_USER, AuditActions.LOGIN))
                .anySatisfy(e -> assertThat(e.getOutcome()).isEqualTo(AuditOutcome.FAILURE));
    }

    @Test
    void unknownUserGetsSameGenericError() throws Exception {
        MvcResult result = login("nobody", "whatever", "10.0.0.2");
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(result.getResponse().getContentAsString()).contains("Usuario o contraseña incorrectos");
    }

    @Test
    void repeatedFailuresBlockTemporarily() throws Exception {
        String ip = "10.0.0.99";
        for (int i = 0; i < 5; i++) {
            assertThat(login(DEMO_USER, "bad-" + i, ip).getResponse().getStatus()).isEqualTo(401);
        }
        MvcResult blocked = login(DEMO_USER, DEMO_PASSWORD, ip);
        assertThat(blocked.getResponse().getStatus()).isEqualTo(429);
        assertThat(blocked.getResponse().getHeader("Retry-After")).isNotBlank();

        // Otra IP no queda afectada.
        assertThat(login(DEMO_USER, DEMO_PASSWORD, "10.0.0.100").getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void loginWithoutCsrfTokenIsForbidden() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidLoginPayloadReturnsValidationProblem() throws Exception {
        MockHttpSession session = new MockHttpSession();
        var csrf = fetchCsrf(session);
        mvc.perform(post("/api/v1/auth/login").session(session).cookie(csrf)
                        .header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
