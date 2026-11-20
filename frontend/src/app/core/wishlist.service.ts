import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from './api';

export interface WishlistItem {
  itemId: string;
  name: string;
  ownerName: string | null;
  wood: string | null;
  core: string | null;
  lengthInches: number | null;
  priceGalleons: number;
  available: boolean;
  stock: number;
  imageUrl: string | null;
  addedAt: string;
}

export interface WishlistResponse {
  items: WishlistItem[];
  totalItems: number;
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private readonly http = inject(HttpClient);
  private readonly wishlistUrl = `${API_BASE_URL}/wishlist`;

  readonly items = signal<WishlistItem[]>([]);
  readonly favoriteIds = signal<ReadonlySet<string>>(new Set());

  load(): Observable<WishlistResponse> {
    return this.http.get<WishlistResponse>(`${this.wishlistUrl}/list`).pipe(
      tap((response) => this.setItems(response.items)),
    );
  }

  add(itemId: string): Observable<WishlistItem> {
    return this.http.post<WishlistItem>(`${this.wishlistUrl}/add`, { itemId }).pipe(
      tap((item) => {
        this.items.update((items) => items.some((saved) => saved.itemId === item.itemId)
          ? items
          : [...items, item]);
        this.favoriteIds.update((ids) => new Set(ids).add(item.itemId));
      }),
    );
  }

  remove(itemId: string): Observable<WishlistResponse> {
    return this.http.post<WishlistResponse>(`${this.wishlistUrl}/remove`, { itemId }).pipe(
      tap(() => this.setItems(this.items().filter((item) => item.itemId !== itemId))),
    );
  }

  private setItems(items: WishlistItem[]): void {
    this.items.set(items);
    this.favoriteIds.set(new Set(items.map((item) => item.itemId)));
  }
}
