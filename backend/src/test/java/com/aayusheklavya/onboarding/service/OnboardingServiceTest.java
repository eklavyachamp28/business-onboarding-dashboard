package com.aayusheklavya.onboarding.service;

import com.aayusheklavya.onboarding.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OnboardingServiceTest {

    @Autowired OnboardingService service;
    @Autowired BusinessApplicationRepository repo;

    @BeforeEach
    void clean() { repo.deleteAll(); }

    private UUID draft() {
        return service.create("Acme Ltd", "LLC", "541511", new BigDecimal("100000")).getId();
    }

    @Test
    void ownershipCannotExceedHundredPercent() {
        UUID id = draft();
        service.addRepresentative(id, "A", "a@x.com", RepresentativeRole.OWNER, new BigDecimal("60"), true);
        assertThatThrownBy(() -> service.addRepresentative(id, "B", "b@x.com", RepresentativeRole.PARTNER, new BigDecimal("50"), false))
                .isInstanceOf(OnboardingException.class)
                .hasMessageContaining("exceeds 100%");
        assertThat(service.get(id).getRepresentatives()).hasSize(1);
    }

    @Test
    void duplicateEmailIsRejected() {
        UUID id = draft();
        service.addRepresentative(id, "A", "a@x.com", RepresentativeRole.OWNER, new BigDecimal("10"), true);
        assertThatThrownBy(() -> service.addRepresentative(id, "A2", "A@X.COM", RepresentativeRole.OWNER, new BigDecimal("10"), false))
                .hasMessageContaining("already exists");
    }

    @Test
    void submitRequiresAnAuthorisedSigner() {
        UUID id = draft();
        assertThatThrownBy(() -> service.submit(id)).hasMessageContaining("at least one representative");
        service.addRepresentative(id, "A", "a@x.com", RepresentativeRole.OWNER, new BigDecimal("100"), false);
        assertThatThrownBy(() -> service.submit(id)).hasMessageContaining("authorised signer");
    }

    @Test
    void unknownNaicsCodeIsRejected() {
        assertThatThrownBy(() -> service.create("X", "LLC", "999999", null)).hasMessageContaining("Unknown NAICS");
    }

    @Test
    void happyPathThroughApproval() {
        UUID id = draft();
        service.addRepresentative(id, "A", "a@x.com", RepresentativeRole.OWNER, new BigDecimal("100"), true);
        assertThat(service.submit(id).getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(service.startReview(id).getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        BusinessApplication approved = service.approve(id, "ok");
        assertThat(approved.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(approved.getReviewNote()).isEqualTo("ok");
        assertThat(approved.getStatus().isFinal()).isTrue();
    }

    @Test
    void illegalTransitionsAreConflicts() {
        UUID id = draft();
        assertThatThrownBy(() -> service.approve(id, null))
                .isInstanceOf(OnboardingException.class)
                .hasMessageContaining("Cannot move application from DRAFT to APPROVED");
        assertThatThrownBy(() -> service.startReview(id)).hasMessageContaining("DRAFT to UNDER_REVIEW");
    }

    @Test
    void rejectionNeedsAReasonAndLocksTheApplication() {
        UUID id = draft();
        service.addRepresentative(id, "A", "a@x.com", RepresentativeRole.OWNER, new BigDecimal("100"), true);
        service.submit(id);
        assertThatThrownBy(() -> service.reject(id, " ")).hasMessageContaining("reason is required");
        service.reject(id, "Missing documents");
        assertThatThrownBy(() -> service.addRepresentative(id, "B", "b@x.com", RepresentativeRole.OWNER, new BigDecimal("0"), false))
                .hasMessageContaining("DRAFT");
    }

    @Test
    void summaryCountsEveryStatusEvenWhenZero() {
        UUID id = draft();
        service.addRepresentative(id, "A", "a@x.com", RepresentativeRole.OWNER, new BigDecimal("100"), true);
        service.submit(id);
        draft();
        var summary = service.summary();
        assertThat(summary).containsEntry(ApplicationStatus.DRAFT, 1L)
                .containsEntry(ApplicationStatus.SUBMITTED, 1L)
                .containsEntry(ApplicationStatus.APPROVED, 0L);
    }
}
