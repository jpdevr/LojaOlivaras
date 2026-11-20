import { Component, computed, effect, inject, signal } from '@angular/core';
import { AdminApi, SalesDashboard } from './admin-api';
import { orderLabels } from '../shared/order-labels';

@Component({
  selector: 'app-admin-sales',
  templateUrl: './admin-sales.html',
})
export class AdminSales {
  private readonly api = inject(AdminApi);
  private readonly dateTime = new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short', timeStyle: 'short', timeZone: 'America/Sao_Paulo',
  });
  readonly number = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
  readonly paymentLabel = orderLabels.payment;
  readonly days = signal(30);
  readonly dashboard = signal<SalesDashboard | null>(null);
  readonly error = signal(false);
  private readonly reloadCount = signal(0);
  readonly maxRevenue = computed(() => Math.max(0, ...(this.dashboard()?.dailySales ?? []).map((day) => day.revenue)));

  constructor() {
    effect((onCleanup) => {
      const days = this.days();
      this.reloadCount();
      this.error.set(false);
      this.dashboard.set(null);
      const request = this.api.sales(days).subscribe({
        next: (dashboard) => this.dashboard.set(dashboard),
        error: () => this.error.set(true),
      });
      onCleanup(() => request.unsubscribe());
    });
  }

  reload(): void {
    this.reloadCount.update((count) => count + 1);
  }

  formatDate(value: string): string {
    return this.dateTime.format(new Date(value));
  }

  formatDay(date: string): string {
    return `${date.slice(8, 10)}/${date.slice(5, 7)}`;
  }
}
