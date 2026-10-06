import { Component, Input } from '@angular/core';
import { ApplicationStatus } from '../core/models';

@Component({
  selector: 'app-status-chip',
  standalone: true,
  template: `<span class="chip" [class]="'chip ' + status.toLowerCase()">{{ label }}</span>`,
  styles: [`
    .chip { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; letter-spacing: .3px; }
    .draft { background: #eceff1; color: #455a64; }
    .submitted { background: #e3f2fd; color: #1565c0; }
    .under_review { background: #fff8e1; color: #ef6c00; }
    .approved { background: #e8f5e9; color: #2e7d32; }
    .rejected { background: #ffebee; color: #c62828; }
  `]
})
export class StatusChipComponent {
  @Input({ required: true }) status!: ApplicationStatus;
  get label(): string { return this.status.replace('_', ' '); }
}
