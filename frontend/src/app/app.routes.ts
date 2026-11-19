import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { AuthService } from './core/auth.service';

const requireLogin: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.getAccessToken() ? true : router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};

const requireCustomer: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.getCurrentUser()()?.role === 'CUSTOMER'
    ? true
    : router.createUrlTree(['/home']);
};

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  {
    path: 'login',
    loadComponent: () => import('./auth-page/auth-page').then((module) => module.AuthPage),
  },
  {
    path: 'home',
    loadComponent: () => import('./home-page/home-page').then((module) => module.HomePage),
  },
  {
    path: 'catalogo',
    loadComponent: () => import('./catalog-page/catalog-page').then((module) => module.CatalogPage),
  },
  {
    path: 'adivinhacao',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./divination-page/divination-page').then((module) => module.DivinationPage),
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
    loadComponent: () => import('./cart-page/cart-page').then((module) => module.CartPage),
  },
  {
    path: 'checkout',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./checkout-page/checkout-page').then((module) => module.CheckoutPage),
  },
  {
    path: 'orders',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./orders-page/orders-page').then((module) => module.OrdersPage),
  },
  {
    path: 'orders/:id/details',
    canActivate: [requireLogin, requireCustomer],
    data: { details: true },
    loadComponent: () => import('./order-page/order-page').then((module) => module.OrderPage),
  },
  {
    path: 'orders/:id',
    canActivate: [requireLogin, requireCustomer],
    loadComponent: () => import('./order-page/order-page').then((module) => module.OrderPage),
  },
  { path: '**', redirectTo: 'home' },
];
