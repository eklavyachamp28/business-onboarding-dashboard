import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { LEGAL_STRUCTURES, LegalStructure, NaicsCode } from '../core/models';
import { OnboardingApiService } from '../core/onboarding-api.service';
import { problemMessage } from '../core/problem';

@Component({
  selector: 'app-new-application',
  standalone: true,
  imports: [AsyncPipe, NgFor, NgIf, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatAutocompleteModule, MatButtonModule],
  templateUrl: './new-application.component.html',
  styles: [`mat-card { max-width: 640px; padding: 24px; } form { display: flex; flex-direction: column; } .row { display: flex; gap: 16px; } mat-form-field { flex: 1; width: 100%; }
            .actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 8px; } .hint { color: #607d8b; font-size: 12px; }`]
})
export class NewApplicationComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(OnboardingApiService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  readonly structures = LEGAL_STRUCTURES;
  saving = false;

  readonly form = this.fb.nonNullable.group({
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    legalStructure: ['LLC' as LegalStructure, Validators.required],
    naicsQuery: [''],
    naicsCode: ['', Validators.pattern(/^\d{6}$/)],
    annualRevenue: [null as number | null, Validators.min(0)]
  });

  /** Debounced NAICS lookup bound to the autocomplete; an empty query shows the first few codes. */
  readonly naicsOptions$: Observable<NaicsCode[]> = this.form.controls.naicsQuery.valueChanges.pipe(
    debounceTime(250),
    distinctUntilChanged(),
    switchMap(q => this.api.searchNaics(q).pipe(catchError(() => of([]))))
  );

  pickNaics(code: NaicsCode): void {
    this.form.controls.naicsCode.setValue(code.code);
    this.form.controls.naicsQuery.setValue(`${code.code} — ${code.title}`, { emitEvent: false });
  }

  displayNaics = (v: string | NaicsCode | null): string => typeof v === 'string' ? v : v ? `${v.code} — ${v.title}` : '';

  submit(): void {
    if (this.form.invalid || this.saving) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    this.saving = true;
    this.api.create({
      businessName: v.businessName.trim(),
      legalStructure: v.legalStructure,
      naicsCode: v.naicsCode || null,
      annualRevenue: v.annualRevenue
    }).subscribe({
      next: app => this.router.navigate(['/applications', app.id]),
      error: err => { this.saving = false; this.snack.open(problemMessage(err), 'Dismiss', { duration: 6000 }); }
    });
  }
}
