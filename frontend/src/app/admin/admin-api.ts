import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { API_BASE_URL } from '../core/api';

export interface DailySales {
  date: string;
  revenue: number;
  units: number;
}

export interface SalesDashboard {
  approvedRevenue: number;
  orderCount: number;
  unitsSold: number;
  dailySales: DailySales[];
  recentOrders: { id: string; createdAt: string; paymentStatus: string; total: number }[];
}

export interface StockItem {
  id: string;
  name: string;
  priceGalleons: number;
  stock: number;
  active: boolean;
}

export interface StockPage {
  items: StockItem[];
  totalPages: number;
  totalItems: number;
}

export interface NewWand {
  name: string;
  description: string;
  priceGalleons: number | null;
  stock: number | null;
  wood: string;
  core: string;
  lengthInches: number | null;
  flexibility: string;
  maker: string;
}

@Injectable({ providedIn: 'root' })
export class AdminApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly url = `${API_BASE_URL}/admin`;

  sales(days: number) {
    return this.http.get<SalesDashboard>(`${this.url}/sales`, { params: { days } })
      .pipe(catchError(this.handleSessionError));
  }

  stock(search: string, page: number) {
    return this.http.get<StockPage>(`${this.url}/inventory`, { params: { search, page } })
      .pipe(catchError(this.handleSessionError));
  }

  createWand(wand: NewWand, images: File[]) {
    const form = new FormData();
    form.append('product', new Blob([JSON.stringify(wand)], { type: 'application/json' }));
    for (const image of images) form.append('images', image);
    return this.http.post<StockItem>(`${this.url}/inventory`, form)
      .pipe(catchError(this.handleSessionError));
  }

  adjustStock(id: string, change: number) {
    return this.http.patch<StockItem>(`${this.url}/inventory/${id}/stock`, { change })
      .pipe(catchError(this.handleSessionError));
  }

  private readonly handleSessionError = (error: unknown) => {
    if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
      this.auth.clearSession();
      void this.router.navigate(['/admin/entrar']);
    }
    return throwError(() => error);
  };
}
