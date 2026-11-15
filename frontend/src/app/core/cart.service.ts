import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface CartItem {
  itemId: string;
  name: string;
  quantity: number;
  unitPriceGalleons: number;
  lineTotalGalleons: number;
  available: boolean;
  imageUrl: string | null;
}

export interface CartResponse {
  items: CartItem[];
  totalGalleons: number;
  totalItems: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly http = inject(HttpClient);
  private readonly cartUrl = 'http://localhost:8080/lojaolivaras/api/cart';

  getCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(`${this.cartUrl}/list`);
  }

  add(itemId: string, quantity = 1): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.cartUrl}/add`, { itemId, quantity });
  }

  remove(itemId: string): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.cartUrl}/remove`, { itemId });
  }

  updateQuantity(itemId: string, quantity: number): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.cartUrl}/update-quantity/${itemId}`, { quantity });
  }

  clear(): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.cartUrl}/clear`, {});
  }
}
