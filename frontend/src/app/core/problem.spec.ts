import { HttpErrorResponse } from '@angular/common/http';
import { problemMessage } from './problem';

describe('problemMessage', () => {
  it('prefers the field error map from a validation problem', () => {
    const err = new HttpErrorResponse({ status: 400, error: { detail: 'Validation failed', errors: { businessName: 'must not be blank' } } });
    expect(problemMessage(err)).toBe('businessName: must not be blank');
  });

  it('falls back to the problem detail text', () => {
    const err = new HttpErrorResponse({ status: 409, error: { detail: 'Cannot move application from DRAFT to APPROVED' } });
    expect(problemMessage(err)).toContain('DRAFT to APPROVED');
  });

  it('explains network failures', () => {
    expect(problemMessage(new HttpErrorResponse({ status: 0 }))).toBe('Cannot reach the API');
  });
});
