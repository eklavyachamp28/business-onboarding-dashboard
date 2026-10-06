import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OnboardingApiService } from './onboarding-api.service';

describe('OnboardingApiService', () => {
  let service: OnboardingApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(OnboardingApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists applications with an optional status filter', () => {
    service.list('APPROVED').subscribe();
    const req = http.expectOne(r => r.url === '/api/applications');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('APPROVED');
    req.flush([]);

    service.list(null).subscribe();
    const all = http.expectOne(r => r.url === '/api/applications');
    expect(all.request.params.has('status')).toBeFalse();
    all.flush([]);
  });

  it('posts workflow transitions with a note only when given', () => {
    service.transition('abc', 'reject', 'Missing docs').subscribe();
    const reject = http.expectOne('/api/applications/abc/reject');
    expect(reject.request.body).toEqual({ note: 'Missing docs' });
    reject.flush({});

    service.transition('abc', 'submit').subscribe();
    const submit = http.expectOne('/api/applications/abc/submit');
    expect(submit.request.body).toEqual({});
    submit.flush({});
  });

  it('searches NAICS codes', () => {
    service.searchNaics('bank').subscribe(r => expect(r.length).toBe(1));
    const req = http.expectOne(r => r.url === '/api/naics');
    expect(req.request.params.get('q')).toBe('bank');
    req.flush([{ code: '522110', title: 'Commercial Banking', sector: 'Finance and Insurance' }]);
  });
});
