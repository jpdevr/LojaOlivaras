import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Observable, finalize, map } from 'rxjs';
import { CatalogItem, CatalogService } from '../core/catalog.service';
import { WishlistService } from '../core/wishlist.service';
import { StoreNavigation } from '../shared/store-navigation';

@Component({
  selector: 'app-catalog-page',
  imports: [FormsModule, StoreNavigation],
  templateUrl: './catalog-page.html',
})
export class CatalogPage {
  private readonly catalog = inject(CatalogService);
  private readonly wishlist = inject(WishlistService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly items = signal<CatalogItem[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly query = signal('');
  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly favoriteIds = this.wishlist.favoriteIds;
  readonly pendingFavorites = signal<ReadonlySet<string>>(new Set());
  readonly wishlistMessage = signal<string | null>(null);

  minPrice: number | null = null;
  maxPrice: number | null = null;
  selectedWood = '';
  selectedCore = '';
  selectedFlexibility = '';
  selectedMaker = '';
  selectedTier = '';
  onlyAvailable = false;

  visibleProducts(): CatalogItem[] {
    return this.items().filter((item) => {
      if (this.minPrice !== null && item.priceGalleons < this.minPrice) return false;
      if (this.maxPrice !== null && item.priceGalleons > this.maxPrice) return false;
      if (this.selectedWood && item.wood !== this.selectedWood) return false;
      if (this.selectedCore && item.core !== this.selectedCore) return false;
      if (this.selectedFlexibility && item.flexibility !== this.selectedFlexibility) return false;
      if (this.selectedMaker && item.maker !== this.selectedMaker) return false;
      if (this.selectedTier && item.canonTier !== this.selectedTier) return false;
      if (this.onlyAvailable && !item.available) return false;
      return true;
    });
  }

  constructor() {
    this.wishlist.load().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      error: () => this.wishlistMessage.set('Não foi possível carregar seus favoritos.'),
    });

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const query = params.get('q')?.trim() ?? '';
      this.query.set(query);
      this.currentPage.set(0);
      this.loadPage(0);
    });
  }

  get woods(): string[] { return this.options((item) => item.wood); }
  get cores(): string[] { return this.options((item) => item.core); }
  get flexibilities(): string[] { return this.options((item) => item.flexibility); }
  get makers(): string[] { return this.options((item) => item.maker); }

  loadPage(page: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    const request = this.query()
      ? this.catalog.search(this.query(), page, 100)
      : this.catalog.list(page, 100);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (response) => {
        this.items.set(response.content);
        this.currentPage.set(response.number);
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Não foi possível carregar o catálogo. Tente novamente.');
        this.loading.set(false);
      },
    });
  }

  clearFilters(): void {
    this.minPrice = null;
    this.maxPrice = null;
    this.selectedWood = '';
    this.selectedCore = '';
    this.selectedFlexibility = '';
    this.selectedMaker = '';
    this.selectedTier = '';
    this.onlyAvailable = false;
  }

  toggleFavorite(itemId: string): void {
    if (this.pendingFavorites().has(itemId)) return;
    this.pendingFavorites.update((ids) => new Set(ids).add(itemId));
    this.wishlistMessage.set(null);

    let request: Observable<void>;
    if (this.favoriteIds().has(itemId)) {
      request = this.wishlist.remove(itemId).pipe(map(() => undefined));
    } else {
      request = this.wishlist.add(itemId).pipe(map(() => undefined));
    }

    request.pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.pendingFavorites.update((ids) => {
        const next = new Set(ids);
        next.delete(itemId);
        return next;
      })),
    ).subscribe({
      error: () => this.wishlistMessage.set('Não foi possível atualizar a wishlist. Tente novamente.'),
    });
  }

  productRows(): CatalogItem[][] {
    const items = this.visibleProducts();
    const rows: CatalogItem[][] = [];
    for (let index = 0; index < items.length; index += 4) {
      rows.push(items.slice(index, index + 4));
    }
    return rows;
  }

  tierName(tier: string): string {
    const labels: Record<string, string> = {
      BOOKS: 'Livros',
      OFFICIAL_SUPPLEMENTAL: 'Material oficial complementar',
      FILM_PRODUCTION: 'Produção cinematográfica',
    };
    return labels[tier] ?? tier;
  }

  private options(selector: (item: CatalogItem) => string | null): string[] {
    return [...new Set(this.items().map(selector).filter((value): value is string => Boolean(value)))]
      .sort((first, second) => first.localeCompare(second, 'pt-BR'));
  }

}
