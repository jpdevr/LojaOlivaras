import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { AuthService } from './core/auth.service';

const requireLogin: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.getAccessToken() ? true : router.createUrlTree(['/login']);
};

const requireCustomer: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.getCurrentUser()()?.role === 'CUSTOMER'
    ? true
    : router.createUrlTree(['/home']);
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
  {
    path: 'catalogo',
    canActivate: [requireLogin],
    loadComponent: () => import('./catalog-page/catalog-page').then((module) => module.CatalogPage),
  },
  {
    path: 'wishlist',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./wishlist-page/wishlist-page').then((module) => module.WishlistPage),
  },
  {
    path: 'profile',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./profile-page/profile-page').then((module) => module.ProfilePage),
  },
  {
    path: 'cart',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./cart-page/cart-page').then((module) => module.CartPage),
  },
  {
    path: 'checkout',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./checkout-page/checkout-page').then((module) => module.CheckoutPage),
  },
  {
    path: 'orders/:id',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./order-page/order-page').then((module) => module.OrderPage),
  },
  { path: '**', redirectTo: 'login' },
];
