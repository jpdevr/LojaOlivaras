import { HttpErrorResponse } from '@angular/common/http';

export const API_BASE_URL = 'http://localhost:8080/lojaolivaras/api';

export function apiErrorMessage(error: unknown, fallback: string, field?: string): string {
  if (!(error instanceof HttpErrorResponse)) return fallback;
  const fieldMessage: unknown = field ? error.error?.fieldErrors?.[field] : null;
  const message: unknown = typeof fieldMessage === 'string' ? fieldMessage : error.error?.message;
  return typeof message === 'string' ? message : fallback;
}
