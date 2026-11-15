import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, catchError, of, switchMap, takeWhile, timer } from 'rxjs';
import { CheckoutService, OrderResponse, PaymentStatusResponse } from '../core/checkout.service';
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
  private statusPolling: Subscription | null = null;

  readonly order = signal<OrderResponse | null>(null);
  readonly payment = signal<PaymentStatusResponse | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly statusError = signal<string | null>(null);
  readonly currentTime = signal(Date.now());
  readonly secondsRemaining = computed(() => {
    const expiresAt = this.payment()?.paymentExpiresAt;
    if (!expiresAt || this.payment()?.paymentStatus !== 'PENDING') return 0;
    return Math.max(0, Math.ceil((Date.parse(expiresAt) - this.currentTime()) / 1000));
  });

  constructor() {
    timer(0, 1000).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.currentTime.set(Date.now()));

    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const orderId = params.get('id');
      if (orderId) this.loadOrder(orderId);
    });
  }

  loadOrder(orderId: string): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.checkout.getOrder(orderId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (order) => {
        this.order.set(order);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível carregar este pedido.'));
        this.loading.set(false);
      },
    });

    this.statusPolling?.unsubscribe();
    this.statusPolling = timer(0, 5000).pipe(
      switchMap(() => this.checkout.getPaymentStatus(orderId).pipe(
        catchError((error: unknown) => {
          this.statusError.set(this.errorText(error, 'Não foi possível atualizar o status do pedido.'));
          return of(null);
        }),
      )),
      takeWhile((status) => status === null || status.orderStatus !== 'DELIVERED', true),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((status) => {
      if (!status) return;
      this.payment.set(status);
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

  downloadBoleto(): void {
    const orderId = this.order()?.id;
    if (!orderId) return;
    this.checkout.downloadBoleto(orderId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (pdf) => {
        const url = URL.createObjectURL(pdf);
        const link = document.createElement('a');
        link.href = url;
        link.download = `boleto-simulado-${orderId}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (error: unknown) => this.statusError.set(this.errorText(error, 'Não foi possível baixar o boleto.')),
    });
  }

  paymentMethodName(method: string): string {
    return ({ PIX: 'PIX', BOLETO: 'Boleto', CREDIT_CARD: 'Cartão de crédito', LEGACY: 'Pedido antigo' })[method] ?? method;
  }

  orderStatusName(status: string): string {
    return ({ CONFIRMED: 'Pedido confirmado', PROCESSING: 'Pagamento concluído; pedido em preparação', DELIVERED: 'Entregue' })[status] ?? status;
  }

  paymentStatusName(status: string): string {
    return ({ PENDING: 'Aguardando conclusão simulada', SIMULATED_APPROVED: 'Aprovado (simulação)' })[status] ?? status;
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
