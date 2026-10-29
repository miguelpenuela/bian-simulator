package com.example.biansimulator.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CatalogElementSpecificationsTest {

    @Test
    void escapesLikeWildcardsAndEscapeCharacter() {
        assertThat(CatalogElementSpecifications.escapeLike("50%_a\\b")).isEqualTo("50\\%\\_a\\\\b");
    }

    @Test
    void leavesPlainTextUntouched() {
        assertThat(CatalogElementSpecifications.escapeLike("payment order")).isEqualTo("payment order");
    }
}
