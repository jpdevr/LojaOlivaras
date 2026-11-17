import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { CatalogItem, CatalogService } from '../core/catalog.service';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-store-navigation',
  imports: [RouterLink],
  templateUrl: './store-navigation.html',
})
export class StoreNavigation {
  private readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly queryChanges = new Subject<string>();

  readonly query = signal('');
  readonly suggestions = signal<CatalogItem[]>([]);
  readonly user = this.auth.getCurrentUser();
  readonly canShop = computed(() => this.user()?.role === 'CUSTOMER');

  constructor() {
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
