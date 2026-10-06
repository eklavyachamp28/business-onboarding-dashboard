package com.aayusheklavya.onboarding.api;

import com.aayusheklavya.onboarding.domain.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Dtos {
    private Dtos() {}

    public record ApplicationRequest(
            @NotBlank @Size(max = 200) String businessName,
            @NotBlank @Pattern(regexp = "SOLE_PROPRIETOR|PARTNERSHIP|LLC|CORPORATION|NON_PROFIT") String legalStructure,
            @Pattern(regexp = "\\d{6}") String naicsCode,
            @PositiveOrZero BigDecimal annualRevenue) {}

    public record RepresentativeRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 200) String email,
            @NotNull RepresentativeRole role,
            @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal ownershipPercent,
            boolean authorisedSigner) {}

    public record DecisionRequest(@Size(max = 500) String note) {}

    public record RepresentativeResponse(UUID id, String fullName, String email, RepresentativeRole role,
                                         BigDecimal ownershipPercent, boolean authorisedSigner) {
        static RepresentativeResponse from(Representative r) {
            return new RepresentativeResponse(r.getId(), r.getFullName(), r.getEmail(), r.getRole(),
                    r.getOwnershipPercent(), r.isAuthorisedSigner());
        }
    }

    public record ApplicationResponse(UUID id, String businessName, String legalStructure, String naicsCode,
                                      BigDecimal annualRevenue, ApplicationStatus status, String reviewNote,
                                      List<ApplicationStatus> allowedTransitions, BigDecimal totalOwnership,
                                      List<RepresentativeResponse> representatives, Instant createdAt, Instant updatedAt) {
        static ApplicationResponse from(BusinessApplication a) {
            return new ApplicationResponse(a.getId(), a.getBusinessName(), a.getLegalStructure(), a.getNaicsCode(),
                    a.getAnnualRevenue(), a.getStatus(), a.getReviewNote(),
                    List.copyOf(a.getStatus().allowedNext()), a.totalOwnership(),
                    a.getRepresentatives().stream().map(RepresentativeResponse::from).toList(),
                    a.getCreatedAt(), a.getUpdatedAt());
        }
    }

    public record ApplicationSummary(UUID id, String businessName, String legalStructure, String naicsCode,
                                     ApplicationStatus status, int representativeCount, Instant updatedAt) {
        static ApplicationSummary from(BusinessApplication a) {
            return new ApplicationSummary(a.getId(), a.getBusinessName(), a.getLegalStructure(), a.getNaicsCode(),
                    a.getStatus(), a.getRepresentatives().size(), a.getUpdatedAt());
        }
    }
}
