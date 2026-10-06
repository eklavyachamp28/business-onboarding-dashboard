package com.aayusheklavya.onboarding.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaicsServiceTest {

    private final NaicsService naics = new NaicsService();

    @Test
    void codePrefixRanksFirst() {
        assertThat(naics.search("5415", 5)).extracting(NaicsCode::code).containsExactly("541511", "541512");
    }

    @Test
    void keywordSearchMatchesTitleWords() {
        assertThat(naics.search("restaurant", 10)).extracting(NaicsCode::code).contains("722511", "722513");
        assertThat(naics.search("software", 10)).extracting(NaicsCode::code).contains("511210", "423430");
    }

    @Test
    void noMatchReturnsEmpty() {
        assertThat(naics.search("zzzz", 10)).isEmpty();
        assertThat(naics.isValid("000000")).isFalse();
        assertThat(naics.isValid("522110")).isTrue();
    }
}
