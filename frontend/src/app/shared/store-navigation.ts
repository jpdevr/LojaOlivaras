import { Component, DestroyRef, ElementRef, computed, inject, input, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { CatalogItem, CatalogService } from '../core/catalog.service';
import { AuthService } from '../core/auth.service';
import { CartService } from '../core/cart.service';
import { StoreIcon } from './store-icon';
import { StoreWordmark } from './store-wordmark';

@Component({
  selector: 'app-store-navigation',
  imports: [NgTemplateOutlet, RouterLink, RouterLinkActive, StoreIcon, StoreWordmark],
  templateUrl: './store-navigation.html',
  host: { '(document:click)': 'closeDisclosuresOutside($event)' },
})
export class StoreNavigation {
  private readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogService);
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly queryChanges = new Subject<string>();
  private readonly themeStorageKey = 'olivaras.house-theme';

  readonly query = signal('');
  readonly suggestions = signal<CatalogItem[]>([]);
  readonly user = this.auth.getCurrentUser();
  readonly cartTotal = this.cart.totalItems;
  readonly cartLinkLabel = computed(() => {
    const total = this.cartTotal();
    if (!total) return 'Carrinho';
    return `Carrinho, ${total} ${total === 1 ? 'item' : 'itens'}`;
  });
  readonly canShop = computed(() => this.user()?.role === 'CUSTOMER');
  readonly showHouseRibbon = input(false);
  readonly pageTitle = input('');
  readonly theme = signal('default');
  readonly houseThemes = [
    { id: 'gryffindor', name: 'Grifinória', crest: '/assets/crests/gryffindor.png' },
    { id: 'slytherin', name: 'Sonserina', crest: '/assets/crests/slytherin.png' },
    { id: 'ravenclaw', name: 'Corvinal', crest: '/assets/crests/ravenclaw.png' },
    { id: 'hufflepuff', name: 'Lufa-Lufa', crest: '/assets/crests/hufflepuff.png' },
  ];
  readonly selectedHouse = computed(() => this.houseThemes.find((house) => house.id === this.theme()));

  constructor() {
    const savedTheme = localStorage.getItem(this.themeStorageKey);
    const knownTheme = this.houseThemes.find((house) => house.id === savedTheme)?.id ?? 'default';
    this.theme.set(knownTheme);
    this.applyTheme(knownTheme);

    this.queryChanges.pipe(
      map((query) => query.trim()),
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((query) => query.length < 2
        ? of([])
        : this.catalog.search(query, 0, 5).pipe(
          map((page) => page.content),
          catchError(() => of([])),
        )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((suggestions) => this.suggestions.set(suggestions));
  }

  setTheme(themeId: string, picker?: HTMLDetailsElement): void {
    const selected = this.houseThemes.find((house) => house.id === themeId);
    if (!selected) return;
    const nextTheme = this.theme() === selected.id ? 'default' : selected.id;
    this.theme.set(nextTheme);
    localStorage.setItem(this.themeStorageKey, nextTheme);
    this.applyTheme(nextTheme);
    if (picker) this.closeMenu(picker);
  }

  closeMenu(menu: HTMLDetailsElement): void {
    menu.open = false;
    menu.querySelector('summary')?.focus();
  }

  closeDisclosuresOutside(event: MouseEvent): void {
    if (!(event.target instanceof Node)) return;
    for (const menu of this.host.nativeElement.querySelectorAll<HTMLDetailsElement>('details[open]')) {
      if (!menu.contains(event.target)) menu.open = false;
    }
  }

  private applyTheme(themeId: string): void {
    document.documentElement.dataset['houseTheme'] = themeId;
  }

  updateQuery(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.query.set(query);
    this.queryChanges.next(query);
  }

  submitSearch(): void {
    const query = this.query().trim();
    this.suggestions.set([]);
    void this.router.navigate(['/catalogo'], {
      queryParams: { q: query || null },
    });
  }

  searchForSuggestion(item: CatalogItem): void {
    this.query.set(item.name);
    this.suggestions.set([]);
    void this.router.navigate(['/catalogo'], { queryParams: { q: item.name } });
  }

  logout(): void {
    this.auth.clearSession();
    void this.router.navigateByUrl('/home');
  }
}
