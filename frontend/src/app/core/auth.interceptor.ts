import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { API_BASE_URL } from './api';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).getAccessToken();
  if (!token || !request.url.startsWith(`${API_BASE_URL}/`)) return next(request);

  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  }));
};
