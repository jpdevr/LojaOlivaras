import { DOCUMENT } from '@angular/common';
import { Component, DestroyRef, ElementRef, effect, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { EMPTY, catchError, finalize, forkJoin, of, switchMap } from 'rxjs';
import { CartService } from '../core/cart.service';
import { CatalogItem, CatalogService } from '../core/catalog.service';
import { WandDetailsService } from '../core/wand-details.service';
import { WandImage } from './wand-image';
import { WandStock } from './wand-stock';

@Component({
  selector: 'app-wand-details-dialog',
  imports: [WandImage, WandStock],
  templateUrl: './wand-details-dialog.html',
})
export class WandDetailsDialog {
  private readonly document = inject(DOCUMENT);
  private readonly details = inject(WandDetailsService);
  private readonly catalog = inject(CatalogService);
  private readonly cart = inject(CartService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly closeButton = viewChild.required<ElementRef<HTMLButtonElement>>('closeButton');
  private readonly lengthFormat = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
  readonly originLabels: Partial<Record<string, string>> = {
    BOOKS: 'Livros',
    OFFICIAL_SUPPLEMENTAL: 'Material oficial complementar',
    FILM_PRODUCTION: 'Produção cinematográfica',
  };

  readonly wand = signal<CatalogItem | null>(null);
  readonly related = signal<CatalogItem[]>([]);
  readonly failed = signal(false);
  readonly adding = signal(false);
  readonly cartMessage = signal('');

  constructor() {
    effect(() => {
      const dialog = this.dialog().nativeElement;
      const open = this.details.itemId() !== null;
      if (open && !dialog.open) {
        dialog.showModal();
        this.document.documentElement.classList.add('wand-dialog-open');
      } else if (!open && dialog.open) {
        dialog.close();
        this.document.documentElement.classList.remove('wand-dialog-open');
      }
    });

    toObservable(this.details.itemId).pipe(
      switchMap((id) => {
        this.wand.set(null);
        this.related.set([]);
        this.failed.set(false);
        this.cartMessage.set('');
        if (!id) return EMPTY;
        return forkJoin({
          wand: this.catalog.detail(id),
          related: this.catalog.related(id).pipe(catchError(() => of([]))),
        }).pipe(catchError(() => {
          this.failed.set(true);
          return EMPTY;
        }));
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(({ wand, related }) => {
      this.wand.set(wand);
      this.related.set(related);
    });
  }

  close(): void {
    this.details.itemId.set(null);
  }

  closeOnCancel(event: Event): void {
    event.preventDefault();
    this.close();
  }

  closeOnBackdrop(event: MouseEvent): void {
    if (event.target === this.dialog().nativeElement) this.close();
  }

  openRelated(id: string): void {
    this.details.itemId.set(id);
    this.dialog().nativeElement.scrollTop = 0;
    this.closeButton().nativeElement.focus();
  }

  formatLength(length: number): string {
    return this.lengthFormat.format(length);
  }

  addToCart(wand: CatalogItem): void {
    if (this.adding() || !wand.available) return;
    this.adding.set(true);
    this.cartMessage.set('');
    this.cart.add(wand.id, 1, wand).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.adding.set(false)),
    ).subscribe({
      next: () => {
        if (this.details.itemId() === wand.id) this.cartMessage.set('Varinha adicionada ao carrinho.');
      },
      error: () => {
        if (this.details.itemId() === wand.id) this.cartMessage.set('Não foi possível adicionar a varinha ao carrinho.');
      },
    });
  }
}
