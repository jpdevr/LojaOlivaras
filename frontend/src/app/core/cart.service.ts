import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, concatMap, from, last, of, tap } from 'rxjs';
import { AuthService } from './auth.service';
import type { CatalogItem } from './catalog.service';

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
  private readonly auth = inject(AuthService);
  private readonly cartUrl = 'http://localhost:8080/lojaolivaras/api/cart';
  private readonly guestCartKey = 'olivaras.guest-cart';

  getCart(): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) return of(this.readGuestCart());
    return this.http.get<CartResponse>(`${this.cartUrl}/list`);
  }

  add(itemId: string, quantity = 1, product?: CatalogItem): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) {
      const cart = this.readGuestCart();
      const existing = cart.items.find((item) => item.itemId === itemId);
      if (existing) existing.quantity = Math.min(99, existing.quantity + quantity);
      else if (product) cart.items.push({
        itemId,
        name: product.name,
        quantity,
        unitPriceGalleons: product.priceGalleons,
        lineTotalGalleons: product.priceGalleons * quantity,
        available: product.available,
        imageUrl: product.imageUrl,
      });
      return of(this.saveGuestCart(cart));
    }
    return this.http.post<CartResponse>(`${this.cartUrl}/add`, { itemId, quantity });
  }

  remove(itemId: string): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) {
      const cart = this.readGuestCart();
      cart.items = cart.items.filter((item) => item.itemId !== itemId);
      return of(this.saveGuestCart(cart));
    }
    return this.http.post<CartResponse>(`${this.cartUrl}/remove`, { itemId });
  }

  updateQuantity(itemId: string, quantity: number): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) {
      const cart = this.readGuestCart();
      const item = cart.items.find((line) => line.itemId === itemId);
      if (item) item.quantity = quantity;
      return of(this.saveGuestCart(cart));
    }
    return this.http.post<CartResponse>(`${this.cartUrl}/update-quantity/${itemId}`, { quantity });
  }

  clear(): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) return of(this.saveGuestCart({ items: [], totalGalleons: 0, totalItems: 0 }));
    return this.http.post<CartResponse>(`${this.cartUrl}/clear`, {});
  }

  mergeGuestCart(): Observable<CartResponse> {
    const guestItems = this.readGuestCart().items;
    if (!guestItems.length || !this.auth.getAccessToken()) {
      return of({ items: [], totalGalleons: 0, totalItems: 0 });
    }

    return from(guestItems).pipe(
      concatMap((item) => this.http.post<CartResponse>(`${this.cartUrl}/add`, {
        itemId: item.itemId,
        quantity: item.quantity,
      })),
      last(),
      tap(() => this.saveGuestCart({ items: [], totalGalleons: 0, totalItems: 0 })),
    );
  }

  private readGuestCart(): CartResponse {
    try {
      const saved = localStorage.getItem(this.guestCartKey);
      const items = saved ? JSON.parse(saved) as CartItem[] : [];
      return this.totals(items);
    } catch {
      return this.totals([]);
    }
  }

  private saveGuestCart(cart: CartResponse): CartResponse {
    const updated = this.totals(cart.items);
    localStorage.setItem(this.guestCartKey, JSON.stringify(updated.items));
    return updated;
  }

  private totals(items: CartItem[]): CartResponse {
    const updatedItems = items.map((item) => ({
      ...item,
      lineTotalGalleons: item.unitPriceGalleons * item.quantity,
    }));
    return {
      items: updatedItems,
      totalGalleons: updatedItems.reduce((total, item) => total + item.lineTotalGalleons, 0),
      totalItems: updatedItems.reduce((total, item) => total + item.quantity, 0),
    };
  }
}
