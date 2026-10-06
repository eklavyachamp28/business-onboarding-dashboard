import { Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MatToolbarModule],
  template: `
    <mat-toolbar color="primary">
      <a routerLink="/" class="brand">Business Banking · Onboarding</a>
    </mat-toolbar>
    <main><router-outlet></router-outlet></main>
  `,
  styles: [`.brand { color: inherit; text-decoration: none; font-weight: 600; } main { max-width: 1100px; margin: 24px auto; padding: 0 16px; }`]
})
export class AppComponent {}
