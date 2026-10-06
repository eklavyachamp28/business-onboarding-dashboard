import { AsyncPipe, CurrencyPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BehaviorSubject, Observable, combineLatest } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { ApplicationDetail, ApplicationStatus, ROLES, RepresentativeRole } from '../core/models';
import { OnboardingApiService } from '../core/onboarding-api.service';
import { problemMessage } from '../core/problem';
import { StatusChipComponent } from '../shared/status-chip.component';

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [AsyncPipe, CurrencyPipe, DatePipe, NgFor, NgIf, ReactiveFormsModule, RouterLink, MatCardModule, MatTableModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatCheckboxModule, MatButtonModule, MatIconModule,
    MatProgressBarModule, StatusChipComponent],
  templateUrl: './application-detail.component.html',
  styleUrl: './application-detail.component.scss'
})
export class ApplicationDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(OnboardingApiService);
  private readonly fb = inject(FormBuilder);
  private readonly snack = inject(MatSnackBar);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  readonly roles = ROLES;
  readonly repColumns = ['fullName', 'email', 'role', 'ownershipPercent', 'authorisedSigner', 'actions'];
  busy = false;

  readonly app$: Observable<ApplicationDetail> = combineLatest([this.route.paramMap, this.refresh$]).pipe(
    map(([params]) => params.get('id')!),
    switchMap(id => this.api.get(id))
  );

  readonly repForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    role: ['OWNER' as RepresentativeRole, Validators.required],
    ownershipPercent: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    authorisedSigner: [false]
  });

  readonly decisionNote = this.fb.nonNullable.control('', Validators.maxLength(500));

  isDraft(app: ApplicationDetail): boolean { return app.status === 'DRAFT'; }
  can(app: ApplicationDetail, next: ApplicationStatus): boolean { return app.allowedTransitions.includes(next); }
  remainingOwnership(app: ApplicationDetail): number { return Math.max(0, 100 - app.totalOwnership); }

  /** Why the submit button is disabled, if it is: mirrors the server-side rules so the user sees them up front. */
  submitBlocker(app: ApplicationDetail): string | null {
    if (app.representatives.length === 0) return 'Add at least one representative';
    if (!app.representatives.some(r => r.authorisedSigner)) return 'Mark at least one representative as an authorised signer';
    if (!app.naicsCode) return 'A NAICS code is required';
    return null;
  }

  addRepresentative(app: ApplicationDetail): void {
    if (this.repForm.invalid || this.busy) { this.repForm.markAllAsTouched(); return; }
    this.run(this.api.addRepresentative(app.id, this.repForm.getRawValue()), () => {
      this.repForm.reset({ role: 'OWNER', ownershipPercent: 0, authorisedSigner: false });
    });
  }

  removeRepresentative(app: ApplicationDetail, repId: string): void {
    this.run(this.api.removeRepresentative(app.id, repId));
  }

  transition(app: ApplicationDetail, action: 'submit' | 'review' | 'approve' | 'reject'): void {
    const note = action === 'approve' || action === 'reject' ? this.decisionNote.value.trim() : undefined;
    this.run(this.api.transition(app.id, action, note), () => this.decisionNote.reset(''));
  }

  private run(call: Observable<unknown>, after?: () => void): void {
    this.busy = true;
    call.subscribe({
      next: () => { this.busy = false; after?.(); this.refresh$.next(); },
      error: err => { this.busy = false; this.snack.open(problemMessage(err), 'Dismiss', { duration: 6000 }); }
    });
  }
}
