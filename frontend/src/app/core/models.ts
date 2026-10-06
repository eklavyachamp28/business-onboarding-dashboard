export type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
export const ALL_STATUSES: ApplicationStatus[] = ['DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED'];

export type RepresentativeRole = 'OWNER' | 'DIRECTOR' | 'PARTNER' | 'OFFICER' | 'AUTHORISED_SIGNER';
export const ROLES: RepresentativeRole[] = ['OWNER', 'DIRECTOR', 'PARTNER', 'OFFICER', 'AUTHORISED_SIGNER'];

export type LegalStructure = 'SOLE_PROPRIETOR' | 'PARTNERSHIP' | 'LLC' | 'CORPORATION' | 'NON_PROFIT';
export const LEGAL_STRUCTURES: LegalStructure[] = ['SOLE_PROPRIETOR', 'PARTNERSHIP', 'LLC', 'CORPORATION', 'NON_PROFIT'];

export interface NaicsCode { code: string; title: string; sector: string; }

export interface Representative {
  id: string;
  fullName: string;
  email: string;
  role: RepresentativeRole;
  ownershipPercent: number;
  authorisedSigner: boolean;
}

export interface ApplicationSummary {
  id: string;
  businessName: string;
  legalStructure: LegalStructure;
  naicsCode: string | null;
  status: ApplicationStatus;
  representativeCount: number;
  updatedAt: string;
}

export interface ApplicationDetail {
  id: string;
  businessName: string;
  legalStructure: LegalStructure;
  naicsCode: string | null;
  annualRevenue: number | null;
  status: ApplicationStatus;
  reviewNote: string | null;
  allowedTransitions: ApplicationStatus[];
  totalOwnership: number;
  representatives: Representative[];
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationRequest {
  businessName: string;
  legalStructure: LegalStructure;
  naicsCode: string | null;
  annualRevenue: number | null;
}

export interface RepresentativeRequest {
  fullName: string;
  email: string;
  role: RepresentativeRole;
  ownershipPercent: number;
  authorisedSigner: boolean;
}

export type StatusSummary = Record<ApplicationStatus, number>;

/** RFC 9457 problem detail as produced by the backend's ApiExceptionHandler. */
export interface ProblemDetail {
  status: number;
  title: string;
  detail: string;
  errors?: Record<string, string>;
}
