CREATE TABLE applications (
    id              UUID PRIMARY KEY,
    business_name   VARCHAR(200)   NOT NULL,
    legal_structure VARCHAR(40)    NOT NULL,
    naics_code      VARCHAR(6),
    annual_revenue  NUMERIC(15, 2),
    status          VARCHAR(20)    NOT NULL,
    review_note     VARCHAR(500),
    created_at      TIMESTAMP      NOT NULL,
    updated_at      TIMESTAMP      NOT NULL,
    version         BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_applications_status ON applications (status, updated_at);

CREATE TABLE representatives (
    id                UUID PRIMARY KEY,
    application_id    UUID          NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    full_name         VARCHAR(120)  NOT NULL,
    email             VARCHAR(200)  NOT NULL,
    role              VARCHAR(30)   NOT NULL,
    ownership_percent NUMERIC(5, 2) NOT NULL,
    authorised_signer BOOLEAN       NOT NULL,
    CONSTRAINT uq_rep_email_per_app UNIQUE (application_id, email)
);
