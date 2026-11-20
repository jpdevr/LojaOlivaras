import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AdminHeader } from './admin-header';

@Component({
  selector: 'app-admin-shell',
  imports: [AdminHeader, RouterOutlet],
  template: `
    <div class="admin-workspace">
      <app-admin-header [showNavigation]="true" />
      <main class="page-shell admin-main">
        <router-outlet />
      </main>
    </div>
  `,
})
export class AdminShell {}
