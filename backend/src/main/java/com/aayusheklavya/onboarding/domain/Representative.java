package com.aayusheklavya.onboarding.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/** A person who can act for the business: owners, directors and authorised signers. */
@Entity
@Table(name = "representatives")
public class Representative {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private BusinessApplication application;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 200)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RepresentativeRole role;

    @Column(name = "ownership_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal ownershipPercent;

    @Column(name = "authorised_signer", nullable = false)
    private boolean authorisedSigner;

    protected Representative() {}

    public Representative(BusinessApplication application, String fullName, String email,
                          RepresentativeRole role, BigDecimal ownershipPercent, boolean authorisedSigner) {
        this.id = UUID.randomUUID();
        this.application = application;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.ownershipPercent = ownershipPercent;
        this.authorisedSigner = authorisedSigner;
    }

    public UUID getId() { return id; }
    public BusinessApplication getApplication() { return application; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public RepresentativeRole getRole() { return role; }
    public BigDecimal getOwnershipPercent() { return ownershipPercent; }
    public boolean isAuthorisedSigner() { return authorisedSigner; }
}
