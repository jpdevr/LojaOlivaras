import { Component, inject } from '@angular/core';
import { StoreNavigation } from '../shared/store-navigation';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-home-page',
  imports: [StoreNavigation],
  templateUrl: './home-page.html',
})
export class HomePage {
  private readonly auth = inject(AuthService);
  readonly user = this.auth.getCurrentUser();
}
