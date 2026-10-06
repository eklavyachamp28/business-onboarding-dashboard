import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApplicationDetail, ApplicationRequest, ApplicationStatus, ApplicationSummary,
  NaicsCode, Representative, RepresentativeRequest, StatusSummary
} from './models';

@Injectable({ providedIn: 'root' })
export class OnboardingApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBase;

  list(status?: ApplicationStatus | null): Observable<ApplicationSummary[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<ApplicationSummary[]>(`${this.base}/applications`, { params });
  }

  summary(): Observable<StatusSummary> {
    return this.http.get<StatusSummary>(`${this.base}/applications/summary`);
  }

  get(id: string): Observable<ApplicationDetail> {
    return this.http.get<ApplicationDetail>(`${this.base}/applications/${id}`);
  }

  create(req: ApplicationRequest): Observable<ApplicationDetail> {
    return this.http.post<ApplicationDetail>(`${this.base}/applications`, req);
  }

  addRepresentative(id: string, req: RepresentativeRequest): Observable<Representative> {
    return this.http.post<Representative>(`${this.base}/applications/${id}/representatives`, req);
  }

  removeRepresentative(id: string, repId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/applications/${id}/representatives/${repId}`);
  }

  transition(id: string, action: 'submit' | 'review' | 'approve' | 'reject', note?: string): Observable<ApplicationDetail> {
    return this.http.post<ApplicationDetail>(`${this.base}/applications/${id}/${action}`, note === undefined ? {} : { note });
  }

  searchNaics(q: string): Observable<NaicsCode[]> {
    return this.http.get<NaicsCode[]>(`${this.base}/naics`, { params: new HttpParams().set('q', q).set('limit', 8) });
  }
}
