import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CartResponse, CartService } from '../core/cart.service';
import { CheckoutService, PaymentMethod } from '../core/checkout.service';
import type { Address } from '../core/profile.service';
import { StoreNavigation } from '../shared/store-navigation';
import { apiErrorMessage } from '../core/api';

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
  private readonly selectedIds = new Set(inject(ActivatedRoute).snapshot.queryParamMap.getAll('item'));

  readonly addresses = signal<Address[]>([]);
  readonly cart = signal<CartResponse | null>(null);
  readonly selectedItems = computed(() => (this.cart()?.items ?? [])
    .filter((item) => this.selectedIds.size === 0 || this.selectedIds.has(item.itemId)));
  readonly selectedTotal = computed(() => this.selectedItems().reduce((total, item) => total + item.lineTotalGalleons, 0));
  readonly canConfirm = computed(() => {
    const items = this.selectedItems();
    return items.length > 0 && (this.selectedIds.size === 0 || this.selectedIds.size === items.length)
      && items.every((item) => item.available);
  });
  readonly loading = signal(true);
  readonly loadingCart = signal(true);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  selectedAddressId = '';
  paymentMethod: PaymentMethod = 'PIX';
  cpf = '';
  cpfTouched = false;

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
        this.errorMessage.set(apiErrorMessage(error, 'Não foi possível carregar os endereços para entrega.'));
        this.loading.set(false);
      },
    });
    this.cartService.getCart().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (cart) => {
        this.cart.set(cart);
        this.loadingCart.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(apiErrorMessage(error, 'Não foi possível carregar o resumo do carrinho.'));
        this.loadingCart.set(false);
      },
    });
  }

  confirmOrder(): void {
    if (this.submitting() || !this.selectedAddressId || !this.canConfirm()) return;
    this.cpfTouched = true;
    if (!this.validCpf()) return;
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.checkout.confirm(this.selectedAddressId, this.paymentMethod, this.cpf.trim(),
      this.selectedItems().map((item) => item.itemId))
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (order) => {
          this.cartService.refreshTotal();
          void this.router.navigate(['/orders', order.id]);
        },
        error: (error: unknown) => {
          this.errorMessage.set(apiErrorMessage(error, 'Não foi possível confirmar o pedido. Confira os dados e tente novamente.', 'cpf'));
          this.submitting.set(false);
        },
      });
  }

  validCpf(): boolean {
    const value = this.cpf.trim();
    if (!/^(?:\d{11}|\d{3}\.\d{3}\.\d{3}-\d{2})$/.test(value)) return false;
    const digits = value.replace(/\D/g, '');
    if (/^(\d)\1{10}$/.test(digits)) return false;

    for (const count of [9, 10]) {
      let sum = 0;
      for (let index = 0; index < count; index++) {
        sum += Number(digits[index]) * (count + 1 - index);
      }
      if ((sum * 10) % 11 % 10 !== Number(digits[count])) return false;
    }
    return true;
  }
}
