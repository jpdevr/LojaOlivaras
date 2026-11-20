import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { AuthService } from './core/auth.service';

const requireLogin: CanActivateFn = (_route, state) => {
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

const requireStoreVisitor: CanActivateFn = () =>
  inject(AuthService).getCurrentUser()()?.role === 'ADMIN'
    ? inject(Router).createUrlTree(['/admin'])
    : true;

const requireAdmin: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.getAccessToken()) return router.createUrlTree(['/admin/entrar']);
  return auth.getCurrentUser()()?.role === 'ADMIN' ? true : router.createUrlTree(['/home']);
};

const requireAdminLoginVisitor: CanActivateFn = () => {
  const user = inject(AuthService).getCurrentUser()();
  return user ? inject(Router).createUrlTree([user.role === 'ADMIN' ? '/admin' : '/home']) : true;
};

const storeRoutes: Routes = [
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

export const routes: Routes = [
  {
    path: 'admin',
    children: [
      {
        path: 'entrar',
        canActivate: [requireAdminLoginVisitor],
        loadComponent: () => import('./admin/admin-login').then((module) => module.AdminLogin),
      },
      {
        path: '',
        canActivate: [requireAdmin],
        canActivateChild: [requireAdmin],
        loadComponent: () => import('./admin/admin-shell').then((module) => module.AdminShell),
        children: [
          {
            path: '',
            pathMatch: 'full',
            loadComponent: () => import('./admin/admin-sales').then((module) => module.AdminSales),
          },
          {
            path: 'estoque',
            loadComponent: () => import('./admin/admin-inventory').then((module) => module.AdminInventory),
          },
        ],
      },
    ],
  },
  { path: '', canActivateChild: [requireStoreVisitor], children: storeRoutes },
];
