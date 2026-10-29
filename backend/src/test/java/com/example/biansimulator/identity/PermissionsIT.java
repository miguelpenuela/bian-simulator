package com.example.biansimulator.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.biansimulator.identity.domain.RoleCode;
import com.example.biansimulator.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/** Verifica la matriz de permisos centralizada contra endpoints reales. */
class PermissionsIT extends AbstractIntegrationTest {

    private static final String PASSWORD = "test-only-Passw0rd!";

    @Test
    void viewerCanReadCatalog() throws Exception {
        String viewer = ensureUser("viewer-it", PASSWORD, RoleCode.VIEWER);
        LoggedIn session = loginAs(viewer, PASSWORD);
        mvc.perform(get("/api/v1/catalog/versions").session(session.session()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").session(session.session()))
                .andExpect(jsonPath("$.permissions.length()").value(2));
    }

    @Test
    void userWithoutRolesIsDenied() throws Exception {
        String noRoles = ensureUser("noroles-it", PASSWORD);
        LoggedIn session = loginAs(noRoles, PASSWORD);
        mvc.perform(get("/api/v1/catalog/versions").session(session.session()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("urn:bian-simulator:problem:forbidden"));
    }

    @Test
    void anonymousIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/catalog/versions")).andExpect(status().isUnauthorized());
    }
}
