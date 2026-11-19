import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, catchError, switchMap, takeWhile, tap, timer } from 'rxjs';
import { CheckoutService, OrderResponse } from '../core/checkout.service';
import { orderLabels } from '../shared/order-labels';
import { StoreNavigation } from '../shared/store-navigation';

@Component({
  selector: 'app-order-page',
  imports: [DatePipe, RouterLink, StoreNavigation],
  templateUrl: './order-page.html',
})
export class OrderPage {
  private readonly checkout = inject(CheckoutService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly showDetails = this.route.snapshot.data['details'] === true;
  readonly labels = orderLabels;
  readonly order = signal<OrderResponse | null>(null);
  readonly qrCode = signal<string | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly statusError = signal<string | null>(null);
  private readonly currentTime = signal(Date.now());
  readonly secondsRemaining = computed(() => {
    const order = this.order();
    if (!order?.paymentExpiresAt || order.paymentStatus !== 'PENDING') return 0;
    return Math.max(0, Math.ceil((Date.parse(order.paymentExpiresAt) - this.currentTime()) / 1000));
  });

  constructor() {
    if (!this.showDetails) {
      timer(0, 1000).pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.currentTime.set(Date.now()));
    }

    this.route.paramMap.pipe(
      switchMap((params) => {
        const orderId = params.get('id');
        if (!orderId) return EMPTY;

        this.loading.set(true);
        this.errorMessage.set(null);
        this.order.set(null);
        this.qrCode.set(null);
        this.statusError.set(null);

        return this.checkout.getOrder(orderId).pipe(
          catchError((error: unknown) => {
            this.errorMessage.set(this.errorText(error, 'Não foi possível carregar este pedido.'));
            this.loading.set(false);
            return EMPTY;
          }),
          tap((order) => {
            this.order.set(order);
            this.loading.set(false);
          }),
          switchMap((order) => this.showDetails || order.status === 'DELIVERED' ? EMPTY : this.pollPayment(orderId)),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((status) => {
      this.qrCode.set(status.qrCodeDataUri);
      this.statusError.set(null);
      this.order.update((order) => order ? {
        ...order,
        status: status.orderStatus,
        paymentStatus: status.paymentStatus,
        paymentExpiresAt: status.paymentExpiresAt,
        paidAt: status.paidAt,
        estimatedDeliveryAt: status.estimatedDeliveryAt,
        deliveredAt: status.deliveredAt,
      } : order);
    });
  }

  private pollPayment(orderId: string) {
    return timer(0, 5000).pipe(
      switchMap(() => this.checkout.getPaymentStatus(orderId).pipe(
        catchError((error: unknown) => {
          this.statusError.set(this.errorText(error, 'Não foi possível atualizar o status do pedido.'));
          return EMPTY;
        }),
      )),
      takeWhile((status) => status.orderStatus !== 'DELIVERED', true),
    );
  }

  downloadBoleto(): void {
    const orderId = this.order()?.id;
    if (!orderId) return;
    this.checkout.downloadBoleto(orderId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (pdf) => {
        const url = URL.createObjectURL(pdf);
        const link = document.createElement('a');
        link.href = url;
        link.download = `boleto-${orderId}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (error: unknown) => this.statusError.set(this.errorText(error, 'Não foi possível baixar o boleto.')),
    });
  }

  formatRemaining(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const remainder = seconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(remainder).padStart(2, '0')}`;
  }

  private errorText(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const message = error.error?.message;
      return typeof message === 'string' ? message : fallback;
    }
    return fallback;
  }
}
