import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { ApplicationDetailComponent } from './application-detail.component';
import { ApplicationDetail } from '../core/models';

const base: ApplicationDetail = {
  id: 'a1', businessName: 'Acme', legalStructure: 'LLC', naicsCode: '541511', annualRevenue: 1000, status: 'DRAFT',
  reviewNote: null, allowedTransitions: ['SUBMITTED'], totalOwnership: 0, representatives: [],
  createdAt: '2026-10-07T00:00:00Z', updatedAt: '2026-10-07T00:00:00Z'
};

describe('ApplicationDetailComponent', () => {
  let component: ApplicationDetailComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ApplicationDetailComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: 'a1' })) } }]
    });
    component = TestBed.createComponent(ApplicationDetailComponent).componentInstance;
  });

  it('explains why a draft cannot be submitted yet', () => {
    expect(component.submitBlocker(base)).toContain('at least one representative');
    const noSigner = { ...base, representatives: [{ id: 'r', fullName: 'A', email: 'a@x.com', role: 'OWNER' as const, ownershipPercent: 100, authorisedSigner: false }] };
    expect(component.submitBlocker(noSigner)).toContain('authorised signer');
    const noNaics = { ...noSigner, naicsCode: null, representatives: [{ ...noSigner.representatives[0], authorisedSigner: true }] };
    expect(component.submitBlocker(noNaics)).toContain('NAICS');
    expect(component.submitBlocker({ ...noNaics, naicsCode: '541511' })).toBeNull();
  });

  it('computes remaining ownership and allowed transitions', () => {
    expect(component.remainingOwnership({ ...base, totalOwnership: 60 })).toBe(40);
    expect(component.remainingOwnership({ ...base, totalOwnership: 100 })).toBe(0);
    expect(component.can(base, 'SUBMITTED')).toBeTrue();
    expect(component.can(base, 'APPROVED')).toBeFalse();
  });

  it('validates the representative form', () => {
    component.repForm.setValue({ fullName: '', email: 'bad', role: 'OWNER', ownershipPercent: 120, authorisedSigner: false });
    expect(component.repForm.invalid).toBeTrue();
    component.repForm.setValue({ fullName: 'Jane', email: 'jane@acme.com', role: 'OWNER', ownershipPercent: 50, authorisedSigner: true });
    expect(component.repForm.valid).toBeTrue();
  });
});
