import { Component, DestroyRef, ElementRef, computed, inject, input, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { Observable, finalize } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { CartService } from '../core/cart.service';
import { CatalogItem } from '../core/catalog.service';
import { WandDetailsService } from '../core/wand-details.service';
import { WishlistItem, WishlistService } from '../core/wishlist.service';
import { WandImage } from './wand-image';
import { flyToCart } from './cart-flight';
import { WandStock } from './wand-stock';

@Component({
  selector: 'app-wand-card',
  imports: [WandImage, WandStock],
  templateUrl: './wand-card.html',
})
export class WandCard {
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly wishlist = inject(WishlistService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly details = inject(WandDetailsService);
  private readonly wandPreview = viewChild.required<WandImage, ElementRef<HTMLElement>>(WandImage, { read: ElementRef });

  readonly item = input.required<CatalogItem | WishlistItem>();
  readonly itemId = computed(() => {
    const item = this.item();
    return 'id' in item ? item.id : item.itemId;
  });
  readonly favorite = computed(() => this.auth.getCurrentUser()()?.role === 'CUSTOMER'
    && this.wishlist.favoriteIds().has(this.itemId()));
  readonly pendingFavorite = signal(false);
  readonly pendingCart = signal(false);
  readonly favoriteMessage = signal<string | null>(null);
  readonly cartAnnouncement = signal('');

  openDetails(): void {
    this.details.itemId.set(this.itemId());
  }

  toggleFavorite(): void {
    if (this.pendingFavorite()) return;
    if (!this.auth.getAccessToken()) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    if (this.auth.getCurrentUser()()?.role !== 'CUSTOMER') {
      this.favoriteMessage.set('Entre com uma conta de cliente para favoritar.');
      return;
    }

    this.pendingFavorite.set(true);
    this.favoriteMessage.set(null);
    const request: Observable<unknown> = this.favorite()
      ? this.wishlist.remove(this.itemId())
      : this.wishlist.add(this.itemId());
    request.pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.pendingFavorite.set(false)),
    ).subscribe({
      error: () => this.favoriteMessage.set('Não foi possível atualizar os favoritos. Tente novamente.'),
    });
  }

  addToCart(): void {
    const item = this.item();
    if (!item.available || this.pendingCart()) return;
    this.pendingCart.set(true);
    this.cartAnnouncement.set('');
    this.cart.add(this.itemId(), 1, item).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.pendingCart.set(false)),
    ).subscribe({
      next: () => {
        this.cartAnnouncement.set(`${item.name} adicionada ao carrinho.`);
        const animation = flyToCart(this.wandPreview().nativeElement);
        if (animation) {
          const cancelOnDestroy = this.destroyRef.onDestroy(() => animation.cancel());
          void animation.finished.then(cancelOnDestroy, cancelOnDestroy);
        }
      },
      error: () => this.cartAnnouncement.set('Não foi possível adicionar ao carrinho.'),
    });
  }
}
