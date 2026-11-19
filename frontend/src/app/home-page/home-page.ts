import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { CatalogItem, CatalogService } from '../core/catalog.service';
import { WishlistService } from '../core/wishlist.service';
import { StoreNavigation } from '../shared/store-navigation';
import { StoreWordmark } from '../shared/store-wordmark';
import { WandCard } from '../shared/wand-card';

@Component({
  selector: 'app-home-page',
  imports: [StoreNavigation, StoreWordmark, RouterLink, WandCard],
  templateUrl: './home-page.html',
})
export class HomePage {
  private readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogService);
  private readonly wishlist = inject(WishlistService);
  private readonly destroyRef = inject(DestroyRef);
  readonly user = this.auth.getCurrentUser();
  readonly featuredWands = signal<CatalogItem[]>([]);
  readonly favoritesError = signal(false);

  constructor() {
    if (this.user()?.role === 'CUSTOMER') {
      this.wishlist.load().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        error: () => this.favoritesError.set(true),
      });
    }
    this.catalog.list(0, 3).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => this.featuredWands.set(page.content),
    });
  }

}
