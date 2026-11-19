import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { CheckoutService, OrderResponse, PageResponse } from '../core/checkout.service';
import { orderLabels } from '../shared/order-labels';
import { StoreNavigation } from '../shared/store-navigation';

@Component({
  selector: 'app-orders-page',
  imports: [DatePipe, RouterLink, StoreNavigation],
  templateUrl: './orders-page.html',
})
export class OrdersPage {
  private readonly checkout = inject(CheckoutService);
  private readonly destroyRef = inject(DestroyRef);
  private requestedPage = 0;

  readonly labels = orderLabels;
  readonly history = signal<PageResponse<OrderResponse> | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.loadOrders();
  }

  loadOrders(page = this.requestedPage): void {
    if (this.loading()) return;
    this.requestedPage = page;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.checkout.listOrders(page).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (history) => this.history.set(history),
      error: (error: unknown) => {
        const message = error instanceof HttpErrorResponse ? error.error?.message : null;
        this.errorMessage.set(typeof message === 'string' ? message : 'Não foi possível carregar seus pedidos.');
      },
    });
  }
}
