package com.example.biansimulator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ApplicationSmokeIT {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void contextStartsAndMigrationsApply() {
        Integer roles = jdbc.queryForObject("select count(*) from role", Integer.class);
        Integer serviceDomains = jdbc.queryForObject(
                "select count(*) from catalog_element where element_type = 'SERVICE_DOMAIN'", Integer.class);
        assertThat(roles).isEqualTo(5);
        assertThat(serviceDomains).isPositive();
    }
}
