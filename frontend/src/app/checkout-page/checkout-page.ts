import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartResponse, CartService } from '../core/cart.service';
import { CheckoutAddress, CheckoutService, PaymentMethod } from '../core/checkout.service';
import { StoreNavigation } from '../shared/store-navigation';

@Component({
  selector: 'app-checkout-page',
  imports: [FormsModule, RouterLink, StoreNavigation],
  templateUrl: './checkout-page.html',
})
export class CheckoutPage {
  private readonly checkout = inject(CheckoutService);
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly addresses = signal<CheckoutAddress[]>([]);
  readonly cart = signal<CartResponse | null>(null);
  readonly loading = signal(true);
  readonly loadingCart = signal(true);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  selectedAddressId = '';
  paymentMethod: PaymentMethod = 'PIX';

  constructor() {
    this.checkout.listAddresses().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (addresses) => {
        this.addresses.set(addresses);
        this.selectedAddressId = addresses.find((address) => address.defaultAddress)?.id
          ?? addresses[0]?.id
          ?? '';
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível carregar os endereços para entrega.'));
        this.loading.set(false);
      },
    });
    this.cartService.getCart().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (cart) => {
        this.cart.set(cart);
        this.loadingCart.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível carregar o resumo do carrinho.'));
        this.loadingCart.set(false);
      },
    });
  }

  confirmOrder(): void {
    if (this.submitting() || !this.selectedAddressId || !this.cart()?.items.length) return;
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.checkout.confirm(this.selectedAddressId, this.paymentMethod)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (order) => void this.router.navigate(['/orders', order.id]),
        error: (error: unknown) => {
          this.errorMessage.set(this.errorText(error, 'Não foi possível confirmar o pedido. Confira o estoque e tente novamente.'));
          this.submitting.set(false);
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
