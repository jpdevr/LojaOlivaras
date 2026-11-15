import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { AuthService } from './core/auth.service';

const requireLogin: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.getAccessToken() ? true : router.createUrlTree(['/login']);
};

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./auth-page/auth-page').then((module) => module.AuthPage),
  },
  {
    path: 'home',
    canActivate: [requireLogin],
    loadComponent: () => import('./home-page/home-page').then((module) => module.HomePage),
  },
  { path: '**', redirectTo: 'login' },
];
