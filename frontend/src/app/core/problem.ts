import { HttpErrorResponse } from '@angular/common/http';
import { ProblemDetail } from './models';

/** Turns an HTTP error into a one-line message for a snackbar, preferring the backend's problem detail. */
export function problemMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const p = err.error as Partial<ProblemDetail> | null;
    if (p?.errors && Object.keys(p.errors).length) {
      return Object.entries(p.errors).map(([f, m]) => `${f}: ${m}`).join('; ');
    }
    if (p?.detail) return p.detail;
    return err.status === 0 ? 'Cannot reach the API' : `Request failed (${err.status})`;
  }
  return 'Unexpected error';
}
