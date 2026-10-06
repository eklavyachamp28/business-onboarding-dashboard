package com.aayusheklavya.onboarding.service;

import com.aayusheklavya.onboarding.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Business rules for onboarding applications. Everything that must hold true for an application
 * (ownership never above 100%, a signer before submission, legal status transitions) lives here,
 * not in the controller.
 */
@Service
public class OnboardingService {

    static final BigDecimal MAX_OWNERSHIP = new BigDecimal("100.00");

    private final BusinessApplicationRepository applications;
    private final NaicsService naics;

    public OnboardingService(BusinessApplicationRepository applications, NaicsService naics) {
        this.applications = applications;
        this.naics = naics;
    }

    @Transactional
    public BusinessApplication create(String businessName, String legalStructure, String naicsCode, BigDecimal annualRevenue) {
        validateNaics(naicsCode);
        return applications.save(new BusinessApplication(businessName, legalStructure, naicsCode, annualRevenue));
    }

    @Transactional
    public BusinessApplication updateDetails(UUID id, String businessName, String legalStructure, String naicsCode, BigDecimal annualRevenue) {
        BusinessApplication app = get(id);
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw OnboardingException.conflict("Only DRAFT applications can be edited");
        }
        validateNaics(naicsCode);
        app.updateDetails(businessName, legalStructure, naicsCode, annualRevenue);
        return app;
    }

    @Transactional(readOnly = true)
    public BusinessApplication get(UUID id) {
        return applications.findById(id).orElseThrow(() -> OnboardingException.notFound("Application " + id));
    }

    @Transactional(readOnly = true)
    public List<BusinessApplication> list(ApplicationStatus status) {
        return status == null ? applications.findAllByOrderByUpdatedAtDesc()
                              : applications.findByStatusOrderByUpdatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> summary() {
        Map<ApplicationStatus, Long> out = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus s : ApplicationStatus.values()) out.put(s, 0L);
        applications.countByStatus().forEach(c -> out.put(c.getStatus(), c.getTotal()));
        return out;
    }

    @Transactional
    public Representative addRepresentative(UUID applicationId, String fullName, String email, RepresentativeRole role,
                                            BigDecimal ownershipPercent, boolean authorisedSigner) {
        BusinessApplication app = get(applicationId);
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw OnboardingException.conflict("Representatives can only be changed while the application is DRAFT");
        }
        boolean duplicate = app.getRepresentatives().stream().anyMatch(r -> r.getEmail().equalsIgnoreCase(email));
        if (duplicate) throw OnboardingException.conflict("A representative with email " + email + " already exists");
        BigDecimal total = app.totalOwnership().add(ownershipPercent);
        if (total.compareTo(MAX_OWNERSHIP) > 0) {
            throw OnboardingException.invalid("Total ownership would be " + total + "%, which exceeds 100%");
        }
        return app.addRepresentative(fullName, email, role, ownershipPercent, authorisedSigner);
    }

    @Transactional
    public void removeRepresentative(UUID applicationId, UUID representativeId) {
        BusinessApplication app = get(applicationId);
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw OnboardingException.conflict("Representatives can only be changed while the application is DRAFT");
        }
        if (!app.removeRepresentative(representativeId)) throw OnboardingException.notFound("Representative " + representativeId);
    }

    @Transactional
    public BusinessApplication submit(UUID id) {
        BusinessApplication app = get(id);
        if (app.getRepresentatives().isEmpty()) throw OnboardingException.invalid("Add at least one representative before submitting");
        if (!app.hasAuthorisedSigner()) throw OnboardingException.invalid("At least one representative must be an authorised signer");
        if (app.getNaicsCode() == null) throw OnboardingException.invalid("NAICS code is required before submitting");
        transition(app, ApplicationStatus.SUBMITTED, null);
        return app;
    }

    @Transactional
    public BusinessApplication startReview(UUID id) {
        BusinessApplication app = get(id);
        transition(app, ApplicationStatus.UNDER_REVIEW, null);
        return app;
    }

    @Transactional
    public BusinessApplication approve(UUID id, String note) {
        BusinessApplication app = get(id);
        transition(app, ApplicationStatus.APPROVED, note);
        return app;
    }

    @Transactional
    public BusinessApplication reject(UUID id, String note) {
        if (note == null || note.isBlank()) throw OnboardingException.invalid("A reason is required when rejecting");
        BusinessApplication app = get(id);
        transition(app, ApplicationStatus.REJECTED, note);
        return app;
    }

    private static void transition(BusinessApplication app, ApplicationStatus next, String note) {
        try {
            app.moveTo(next, note);
        } catch (IllegalStateException e) {
            throw OnboardingException.conflict(e.getMessage());
        }
    }

    private void validateNaics(String code) {
        if (code != null && !naics.isValid(code)) throw OnboardingException.invalid("Unknown NAICS code: " + code);
    }
}
