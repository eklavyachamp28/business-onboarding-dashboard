import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent), title: 'Onboarding dashboard' },
  { path: 'applications/new', loadComponent: () => import('./new-application/new-application.component').then(m => m.NewApplicationComponent), title: 'New application' },
  { path: 'applications/:id', loadComponent: () => import('./application-detail/application-detail.component').then(m => m.ApplicationDetailComponent), title: 'Application' },
  { path: '**', redirectTo: '' }
];
