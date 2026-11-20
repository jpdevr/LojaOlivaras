import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { Observable, finalize } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { CartItem, CartResponse, CartService } from '../core/cart.service';
import { StoreNavigation } from '../shared/store-navigation';
import { WandImage } from '../shared/wand-image';
import { apiErrorMessage } from '../core/api';

@Component({
  selector: 'app-cart-page',
  imports: [RouterLink, StoreNavigation, WandImage],
  templateUrl: './cart-page.html',
})
export class CartPage {
  private readonly cartService = inject(CartService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly uncheckedIds = signal<ReadonlySet<string>>(new Set());

  readonly cart = signal<CartResponse | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly working = signal(false);
  readonly isCustomer = computed(() => this.auth.getCurrentUser()()?.role === 'CUSTOMER');
  readonly transferNotice = this.cartService.transferNotice;
  readonly transferNeedsRetry = this.cartService.transferNeedsRetry;
  readonly availableItems = computed(() => this.cart()?.items.filter((item) => item.available) ?? []);
  readonly selectedItems = computed(() => this.availableItems().filter((item) => this.isSelected(item)));
  readonly selectedQuantity = computed(() => this.selectedItems().reduce((total, item) => total + item.quantity, 0));
  readonly selectedTotal = computed(() => this.selectedItems().reduce((total, item) => total + item.lineTotalGalleons, 0));
  readonly allSelected = computed(() => this.availableItems().length > 0
    && this.selectedItems().length === this.availableItems().length);

  constructor() { this.loadCart(); }

  loadCart(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.cartService.getCart().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (cart) => this.cart.set(cart),
      error: (error: unknown) => this.showError(error, 'Não foi possível carregar seu carrinho.'),
    });
  }

  isSelected(item: CartItem): boolean {
    return item.available && !this.uncheckedIds().has(item.itemId);
  }

  toggleItem(item: CartItem): void {
    if (!item.available) return;
    const next = new Set(this.uncheckedIds());
    if (next.has(item.itemId)) next.delete(item.itemId);
    else next.add(item.itemId);
    this.uncheckedIds.set(next);
  }

  toggleAll(): void {
    this.uncheckedIds.set(this.allSelected()
      ? new Set(this.availableItems().map((item) => item.itemId))
      : new Set());
  }

  changeQuantity(item: CartItem, delta: number): void {
    const quantity = item.quantity + delta;
    if (quantity < 1 || quantity > 99) return;
    this.mutate(() => this.cartService.updateQuantity(item.itemId, quantity));
  }

  remove(item: CartItem): void {
    this.mutate(() => this.cartService.remove(item.itemId));
  }

  clear(): void {
    this.mutate(() => this.cartService.clear());
  }

  retryTransfer(): void {
    if (this.working()) return;
    this.working.set(true);
    this.cartService.mergeGuestCart().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.working.set(false)),
    ).subscribe({
      next: () => this.loadCart(),
      error: () => { /* O serviço mantém os itens e atualiza o aviso de transferência. */ },
    });
  }

  continueCheckout(): void {
    if (this.working() || !this.selectedItems().length) return;
    void this.router.navigate(['/checkout'], {
      queryParams: { item: this.selectedItems().map((item) => item.itemId) },
    });
  }

  private mutate(request: () => Observable<CartResponse>): void {
    if (this.working()) return;
    this.working.set(true);
    this.errorMessage.set(null);
    request().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.working.set(false)),
    ).subscribe({
      next: (cart) => {
        this.cart.set(cart);
        const remainingIds = new Set(cart.items.map((item) => item.itemId));
        this.uncheckedIds.update((ids) => new Set([...ids].filter((id) => remainingIds.has(id))));
      },
      error: (error: unknown) => this.showError(error, 'Não foi possível atualizar o carrinho.'),
    });
  }

  private showError(error: unknown, fallback: string): void {
    this.errorMessage.set(apiErrorMessage(error, fallback));
  }
}
