import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, map, startWith, switchMap, tap } from 'rxjs/operators';
import { ALL_STATUSES, ApplicationStatus, ApplicationSummary, StatusSummary } from '../core/models';
import { OnboardingApiService } from '../core/onboarding-api.service';
import { StatusChipComponent } from '../shared/status-chip.component';

interface DashboardVm {
  summary: StatusSummary | null;
  rows: ApplicationSummary[];
  error: string | null;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [AsyncPipe, DatePipe, NgFor, NgIf, ReactiveFormsModule, RouterLink, MatCardModule, MatTableModule,
    MatSelectModule, MatFormFieldModule, MatButtonModule, MatIconModule, MatProgressBarModule, StatusChipComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private readonly api = inject(OnboardingApiService);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  readonly statuses = ALL_STATUSES;
  readonly statusFilter = new FormControl<ApplicationStatus | null>(null);
  readonly columns = ['businessName', 'legalStructure', 'naicsCode', 'representativeCount', 'status', 'updatedAt'];
  loading = false;

  /** One view-model stream: status filter changes and manual refreshes both re-query the API. */
  readonly vm$: Observable<DashboardVm> = combineLatest([
    this.statusFilter.valueChanges.pipe(startWith(this.statusFilter.value)),
    this.refresh$
  ]).pipe(
    tap(() => this.loading = true),
    switchMap(([status]) => combineLatest([this.api.summary(), this.api.list(status)]).pipe(
      map(([summary, rows]) => ({ summary, rows, error: null } as DashboardVm)),
      catchError(() => of({ summary: null, rows: [], error: 'Could not load applications' } as DashboardVm))
    )),
    tap(() => this.loading = false)
  );

  reload(): void { this.refresh$.next(); }

  total(summary: StatusSummary): number {
    return Object.values(summary).reduce((a, b) => a + b, 0);
  }

  setFilter(status: ApplicationStatus): void {
    this.statusFilter.setValue(this.statusFilter.value === status ? null : status);
  }
}
