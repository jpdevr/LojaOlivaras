import { Component, inject, input } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { StoreIcon } from '../shared/store-icon';
import { StoreWordmark } from '../shared/store-wordmark';

@Component({
  selector: 'app-admin-header',
  imports: [RouterLink, RouterLinkActive, StoreIcon, StoreWordmark],
  template: `
    <header class="store-header admin-header">
      <div class="store-lanterns" aria-hidden="true">
        <span class="store-lantern store-lantern-left"></span>
        <span class="store-lantern store-lantern-right"></span>
      </div>
      <div class="store-brand-row">
        <div class="store-brand">
          <span class="brand-ornament" aria-hidden="true">✦</span>
          <app-store-wordmark />
          <span class="brand-ornament" aria-hidden="true">✦</span>
        </div>
        <span class="store-page-title">Administração <span class="brand-ornament" aria-hidden="true">✦</span></span>
      </div>
      @if (showNavigation()) {
        <nav class="store-nav etched-control" aria-label="Navegação administrativa">
          <div class="store-links">
            <a class="store-nav-link etched-control" routerLink="/admin" routerLinkActive="active"
               ariaCurrentWhenActive="page" [routerLinkActiveOptions]="{ exact: true }">
              <app-store-icon name="orders" /> Vendas
            </a>
            <a class="store-nav-link etched-control" routerLink="/admin/estoque" routerLinkActive="active"
               ariaCurrentWhenActive="page">
              <app-store-icon name="wand" /> Estoque
            </a>
          </div>
          <button class="store-nav-link" type="button" (click)="signOut()">
            <app-store-icon name="sign-out" /> Sair
          </button>
        </nav>
      }
    </header>
  `,
})
export class AdminHeader {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly showNavigation = input(false);

  signOut(): void {
    this.auth.clearSession();
    void this.router.navigate(['/admin/entrar']);
  }
}
