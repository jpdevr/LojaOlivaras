import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { CartItem, CartResponse, CartService } from '../core/cart.service';
import { StoreNavigation } from '../shared/store-navigation';

@Component({
  selector: 'app-cart-page',
  imports: [RouterLink, StoreNavigation],
  templateUrl: './cart-page.html',
})
export class CartPage {
  private readonly cartService = inject(CartService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cart = signal<CartResponse | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly pendingItems = signal<ReadonlySet<string>>(new Set());
  readonly clearing = signal(false);

  constructor() { this.loadCart(); }

  loadCart(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.cartService.getCart().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (cart) => {
        this.cart.set(cart);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível carregar seu carrinho.'));
        this.loading.set(false);
      },
    });
  }

  updateQuantity(item: CartItem, event: Event): void {
    const quantity = Number((event.target as HTMLInputElement).value);
    if (!Number.isInteger(quantity) || quantity < 1 || quantity > 99) {
      this.errorMessage.set('A quantidade deve ser um número entre 1 e 99.');
      return;
    }
    this.mutate(item.itemId, this.cartService.updateQuantity(item.itemId, quantity));
  }

  remove(item: CartItem): void {
    this.mutate(item.itemId, this.cartService.remove(item.itemId));
  }

  clear(): void {
    if (this.clearing()) return;
    this.clearing.set(true);
    this.errorMessage.set(null);
    this.cartService.clear().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (cart) => {
        this.cart.set(cart);
        this.clearing.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível esvaziar o carrinho.'));
        this.clearing.set(false);
      },
    });
  }

  private mutate(itemId: string, request: import('rxjs').Observable<CartResponse>): void {
    if (this.pendingItems().has(itemId)) return;
    this.pendingItems.update((ids) => new Set(ids).add(itemId));
    this.errorMessage.set(null);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (cart) => {
        this.cart.set(cart);
        this.pendingItems.update((ids) => {
          const next = new Set(ids);
          next.delete(itemId);
          return next;
        });
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível atualizar o carrinho.'));
        this.pendingItems.update((ids) => {
          const next = new Set(ids);
          next.delete(itemId);
          return next;
        });
      },
    });
  }

  private errorText(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const message = error.error?.message;
      return typeof message === 'string' ? message : fallback;
    }
    return fallback;
  }
}
