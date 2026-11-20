import { DOCUMENT } from '@angular/common';
import { ApplicationConfig, inject, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withViewTransitions } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideRouter(routes, withViewTransitions({
      skipInitialTransition: true,
      onViewTransitionCreated: ({ transition, from, to }) => {
        const isAdmin = from.firstChild?.routeConfig?.path === 'admin'
          || to.firstChild?.routeConfig?.path === 'admin';
        if (isAdmin) {
          const root = inject(DOCUMENT).documentElement;
          root.classList.add('admin-transition');
          const clearAdminTransition = () => root.classList.remove('admin-transition');
          void transition.finished.then(clearAdminTransition, clearAdminTransition);
        } else if (matchMedia('(prefers-reduced-motion: reduce)').matches) {
          transition.skipTransition();
        }
      },
    })),
  ]
};
