package com.aayusheklavya.onboarding.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of a business onboarding application. Transitions are explicit so that
 * an invalid move (e.g. approving a DRAFT) is rejected rather than silently applied.
 */
public enum ApplicationStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED;

    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case DRAFT -> EnumSet.of(SUBMITTED);
            case SUBMITTED -> EnumSet.of(UNDER_REVIEW, REJECTED);
            case UNDER_REVIEW -> EnumSet.of(APPROVED, REJECTED);
            case APPROVED, REJECTED -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canMoveTo(ApplicationStatus next) {
        return allowedNext().contains(next);
    }

    public boolean isFinal() {
        return allowedNext().isEmpty();
    }
}
