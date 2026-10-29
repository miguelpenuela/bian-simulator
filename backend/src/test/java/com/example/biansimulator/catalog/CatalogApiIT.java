package com.example.biansimulator.catalog;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.biansimulator.support.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;

class CatalogApiIT extends AbstractIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    MockHttpSession session;

    @BeforeEach
    void authenticate() throws Exception {
        session = loginAs(DEMO_USER, DEMO_PASSWORD).session();
    }

    private UUID idOf(String externalRef) {
        return jdbc.queryForObject("select id from catalog_element where external_ref = ?", UUID.class,
                externalRef);
    }

    @Test
    void demoCatalogIsFlaggedAsNotOfficial() throws Exception {
        mvc.perform(get("/api/v1/catalog/versions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("DEMO-EDU-1"))
                .andExpect(jsonPath("$[0].sourceType").value("EDUCATIONAL_DEMO"))
                .andExpect(jsonPath("$[0].official").value(false));
    }

    @Test
    void searchFiltersByTypeAndText() throws Exception {
        mvc.perform(get("/api/v1/catalog/elements").session(session)
                        .param("type", "SERVICE_DOMAIN").param("q", "payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].type", everyItem(is("SERVICE_DOMAIN"))))
                .andExpect(jsonPath("$.content[*].name", hasItem("Payment Order")))
                .andExpect(jsonPath("$.content[*].validationStatus", everyItem(is("UNVERIFIED"))));
    }

    @Test
    void searchMatchesDescriptionCaseInsensitively() throws Exception {
        mvc.perform(get("/api/v1/catalog/elements").session(session)
                        .param("type", "SERVICE_DOMAIN").param("q", "FRAUDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem("Fraud Evaluation")));
    }

    @Test
    void likeWildcardsInQueryAreTreatedLiterally() throws Exception {
        mvc.perform(get("/api/v1/catalog/elements").session(session).param("q", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void searchFiltersByCategoryAndPaginates() throws Exception {
        mvc.perform(get("/api/v1/catalog/elements").session(session)
                        .param("category", "Pagos").param("type", "SERVICE_OPERATION").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void pageSizeIsBounded() throws Exception {
        mvc.perform(get("/api/v1/catalog/elements").session(session).param("size", "500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidTypeIsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/catalog/elements").session(session).param("type", "NOPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void facetsCountTypesAndCategories() throws Exception {
        UUID versionId = jdbc.queryForObject("select id from catalog_version where code = 'DEMO-EDU-1'",
                UUID.class);
        mvc.perform(get("/api/v1/catalog/versions/{id}/facets", versionId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types[?(@.value == 'SERVICE_DOMAIN')].count").value(hasItem(18)))
                .andExpect(jsonPath("$.categories.length()", greaterThan(3)));
    }

    @Test
    void serviceDomainDetailIncludesRelationshipsWithProvenance() throws Exception {
        UUID paymentOrder = idOf("SD-PAYMENT-ORDER");
        mvc.perform(get("/api/v1/catalog/service-domains/{id}", paymentOrder).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Payment Order"))
                .andExpect(jsonPath("$.version.official").value(false))
                .andExpect(jsonPath("$.relationships[?(@.relationshipType == 'HAS_OPERATION')]").isNotEmpty())
                .andExpect(jsonPath("$.relationships[?(@.relationshipType == 'CONTAINS')].direction")
                        .value(hasItem("INCOMING")))
                .andExpect(jsonPath("$.relationships[*].provenance", everyItem(is("EDUCATIONAL_EXAMPLE"))));
    }

    @Test
    void serviceDomainEndpointRejectsOtherElementTypes() throws Exception {
        UUID operation = idOf("SD-PAYMENT-ORDER-OP-INITIATE");
        mvc.perform(get("/api/v1/catalog/service-domains/{id}", operation).session(session))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/catalog/elements/{id}", operation).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SERVICE_OPERATION"));
    }

    @Test
    void unknownElementIs404Problem() throws Exception {
        mvc.perform(get("/api/v1/catalog/elements/{id}", UUID.randomUUID()).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:bian-simulator:problem:not-found"));
    }
}
