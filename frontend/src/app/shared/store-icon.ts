import { Component, input } from '@angular/core';

type StoreIconName = 'home' | 'catalog' | 'cart' | 'profile' | 'wishlist' | 'orders'
  | 'sign-in' | 'register' | 'sign-out' | 'shield' | 'search';

@Component({
  selector: 'app-store-icon',
  template: `
    <svg viewBox="0 0 32 32" aria-hidden="true" focusable="false">
      <use [attr.href]="'/assets/icons/store-menu.svg#' + name()" />
    </svg>
  `,
})
export class StoreIcon {
  readonly name = input.required<StoreIconName>();
}
