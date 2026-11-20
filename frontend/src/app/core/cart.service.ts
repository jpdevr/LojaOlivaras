import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { EMPTY, Observable, Subject, catchError, finalize, map, of, startWith, switchMap, tap } from 'rxjs';
import { AuthService } from './auth.service';
import type { CatalogItem } from './catalog.service';
import { API_BASE_URL } from './api';

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

interface GuestCartDraft {
  transferId?: string;
  items: CartItem[];
}

interface GuestCartTransfer {
  skippedUnits: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly cartUrl = `${API_BASE_URL}/cart`;
  private readonly guestCartKey = 'olivaras.guest-cart';
  private readonly totalRefresh = new Subject<void>();
  private readonly customerSession = computed(() => this.auth.getCurrentUser()()?.role === 'CUSTOMER'
    ? this.auth.getAccessToken()
    : null);
  private readonly pendingTransferMessage = 'Os itens de visitante foram guardados. Você pode tentar transferi-los novamente.';
  readonly transferNeedsRetry = signal(Boolean(this.readGuestDraft().transferId));
  readonly transferNotice = signal<string | null>(this.transferNeedsRetry() ? this.pendingTransferMessage : null);

  readonly totalItems = toSignal(toObservable(this.customerSession).pipe(
    switchMap((session) => this.totalRefresh.pipe(
      startWith(undefined),
      // A new refresh supersedes any read started before the latest cart change.
      switchMap(() => session
        ? this.http.get<Pick<CartResponse, 'totalItems'>>(`${this.cartUrl}/total`).pipe(
          map((cart) => cart.totalItems),
          catchError(() => EMPTY),
        )
        : of(this.readGuestCart().totalItems)),
      startWith(session ? 0 : this.readGuestCart().totalItems),
    )),
  ), { initialValue: this.readGuestCart().totalItems });

  refreshTotal(): void {
    this.totalRefresh.next();
  }

  getCart(): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) return of(this.readGuestCart());
    return this.http.get<CartResponse>(`${this.cartUrl}/list`);
  }

  add(itemId: string, quantity = 1, product?: Pick<CatalogItem, 'name' | 'priceGalleons' | 'available' | 'imageUrl'>): Observable<CartResponse> {
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
    return this.refreshAfter(this.http.post<CartResponse>(`${this.cartUrl}/add`, { itemId, quantity }));
  }

  remove(itemId: string): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) {
      const cart = this.readGuestCart();
      cart.items = cart.items.filter((item) => item.itemId !== itemId);
      return of(this.saveGuestCart(cart));
    }
    return this.refreshAfter(this.http.post<CartResponse>(`${this.cartUrl}/remove`, { itemId }));
  }

  updateQuantity(itemId: string, quantity: number): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) {
      const cart = this.readGuestCart();
      const item = cart.items.find((line) => line.itemId === itemId);
      if (item) item.quantity = quantity;
      return of(this.saveGuestCart(cart));
    }
    return this.refreshAfter(this.http.post<CartResponse>(`${this.cartUrl}/update-quantity/${itemId}`, { quantity }));
  }

  clear(): Observable<CartResponse> {
    if (!this.auth.getAccessToken()) return of(this.saveGuestCart({ items: [], totalGalleons: 0, totalItems: 0 }));
    return this.refreshAfter(this.http.post<CartResponse>(`${this.cartUrl}/clear`, {}));
  }

  mergeGuestCart(): Observable<GuestCartTransfer> {
    const draft = this.readGuestDraft();
    this.transferNotice.set(null);
    this.transferNeedsRetry.set(false);
    if (!draft.items.length || this.auth.getCurrentUser()()?.role !== 'CUSTOMER') {
      return of({ skippedUnits: 0 });
    }

    const transferId = draft.transferId ?? crypto.randomUUID();
    localStorage.setItem(this.guestCartKey, JSON.stringify({ ...draft, transferId }));
    const items = draft.items.map(({ itemId, quantity }) => ({ itemId, quantity }));
    return this.http.post<GuestCartTransfer>(`${this.cartUrl}/merge`, { transferId, items }).pipe(
      tap({
        next: ({ skippedUnits }) => {
          if (this.readGuestDraft().transferId === transferId) localStorage.removeItem(this.guestCartKey);
          if (skippedUnits) {
            this.transferNotice.set(`Carrinho transferido. Unidades indisponíveis: ${skippedUnits}.`);
          }
        },
        error: () => {
          this.transferNeedsRetry.set(true);
          this.transferNotice.set(this.pendingTransferMessage);
        },
      }),
      finalize(() => this.refreshTotal()),
    );
  }

  private refreshAfter(request: Observable<CartResponse>): Observable<CartResponse> {
    return request.pipe(tap(() => this.refreshTotal()));
  }

  private readGuestCart(): CartResponse {
    return this.totals(this.readGuestDraft().items);
  }

  private readGuestDraft(): GuestCartDraft {
    try {
      const saved = localStorage.getItem(this.guestCartKey);
      const data = saved ? JSON.parse(saved) as GuestCartDraft | CartItem[] : [];
      if (Array.isArray(data)) return { items: data };
      return Array.isArray(data?.items) ? data : { items: [] };
    } catch {
      return { items: [] };
    }
  }

  private saveGuestCart(cart: CartResponse): CartResponse {
    const updated = this.totals(cart.items);
    localStorage.setItem(this.guestCartKey, JSON.stringify(updated.items));
    this.refreshTotal();
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
