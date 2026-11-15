import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { WishlistItem, WishlistService } from '../core/wishlist.service';
import { StoreNavigation } from '../shared/store-navigation';

type WishlistSort = 'name' | 'price-ascending' | 'price-descending';

@Component({
  selector: 'app-wishlist-page',
  imports: [FormsModule, StoreNavigation],
  templateUrl: './wishlist-page.html',
})
export class WishlistPage {
  private readonly wishlist = inject(WishlistService);
  private readonly destroyRef = inject(DestroyRef);

  readonly items = this.wishlist.items;
  readonly loading = signal(true);
  readonly sortBy = signal<WishlistSort>('name');
  readonly errorMessage = signal<string | null>(null);
  readonly pendingRemovals = signal<ReadonlySet<string>>(new Set());

  readonly sortedItems = computed(() => {
    const items = [...this.items()];
    switch (this.sortBy()) {
      case 'price-ascending':
        return items.sort((first, second) => first.priceGalleons - second.priceGalleons
          || first.name.localeCompare(second.name, 'pt-BR'));
      case 'price-descending':
        return items.sort((first, second) => second.priceGalleons - first.priceGalleons
          || first.name.localeCompare(second.name, 'pt-BR'));
      default:
        return items.sort((first, second) => first.name.localeCompare(second.name, 'pt-BR'));
    }
  });

  constructor() {
    this.wishlist.load().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => this.loading.set(false),
      error: () => {
        this.errorMessage.set('Não foi possível carregar sua wishlist. Tente novamente.');
        this.loading.set(false);
      },
    });
  }

  remove(item: WishlistItem): void {
    if (this.pendingRemovals().has(item.itemId)) return;
    this.pendingRemovals.update((ids) => new Set(ids).add(item.itemId));
    this.errorMessage.set(null);

    this.wishlist.remove(item.itemId).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.pendingRemovals.update((ids) => {
        const next = new Set(ids);
        next.delete(item.itemId);
        return next;
      })),
    ).subscribe({
      error: () => this.errorMessage.set(`Não foi possível remover “${item.name}” dos favoritos.`),
    });
  }

  retry(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.wishlist.load().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => this.loading.set(false),
      error: () => {
        this.errorMessage.set('Não foi possível carregar sua wishlist. Tente novamente.');
        this.loading.set(false);
      },
    });
  }
}
