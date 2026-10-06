package com.aayusheklavya.onboarding.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "applications")
public class BusinessApplication {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "legal_structure", nullable = false, length = 40)
    private String legalStructure;

    @Column(name = "naics_code", length = 6)
    private String naicsCode;

    @Column(name = "annual_revenue", precision = 15, scale = 2)
    private BigDecimal annualRevenue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Representative> representatives = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected BusinessApplication() {}

    public BusinessApplication(String businessName, String legalStructure, String naicsCode, BigDecimal annualRevenue) {
        this.id = UUID.randomUUID();
        this.businessName = businessName;
        this.legalStructure = legalStructure;
        this.naicsCode = naicsCode;
        this.annualRevenue = annualRevenue;
        this.status = ApplicationStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Representative addRepresentative(String fullName, String email, RepresentativeRole role,
                                            BigDecimal ownershipPercent, boolean authorisedSigner) {
        Representative r = new Representative(this, fullName, email, role, ownershipPercent, authorisedSigner);
        representatives.add(r);
        touch();
        return r;
    }

    public boolean removeRepresentative(UUID representativeId) {
        boolean removed = representatives.removeIf(r -> r.getId().equals(representativeId));
        if (removed) touch();
        return removed;
    }

    public BigDecimal totalOwnership() {
        return representatives.stream().map(Representative::getOwnershipPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean hasAuthorisedSigner() {
        return representatives.stream().anyMatch(Representative::isAuthorisedSigner);
    }

    public void moveTo(ApplicationStatus next, String note) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException("Cannot move application from " + status + " to " + next);
        }
        this.status = next;
        this.reviewNote = note;
        touch();
    }

    public void updateDetails(String businessName, String legalStructure, String naicsCode, BigDecimal annualRevenue) {
        this.businessName = businessName;
        this.legalStructure = legalStructure;
        this.naicsCode = naicsCode;
        this.annualRevenue = annualRevenue;
        touch();
    }

    private void touch() { this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getBusinessName() { return businessName; }
    public String getLegalStructure() { return legalStructure; }
    public String getNaicsCode() { return naicsCode; }
    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    public ApplicationStatus getStatus() { return status; }
    public String getReviewNote() { return reviewNote; }
    public List<Representative> getRepresentatives() { return representatives; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
