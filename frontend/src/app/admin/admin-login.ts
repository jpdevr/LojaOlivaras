import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { AdminHeader } from './admin-header';

@Component({
  selector: 'app-admin-login',
  imports: [AdminHeader, FormsModule, RouterLink],
  template: `
    <div class="admin-workspace">
      <app-admin-header />
      <main class="admin-login">
        <form class="arch-frame admin-panel admin-login-form" (ngSubmit)="submit()" #loginForm="ngForm">
          <h1>Acesso administrativo</h1>
          <label>E-mail <input type="email" name="email" autocomplete="username" [(ngModel)]="email" required email /></label>
          <label>Senha <input type="password" name="password" autocomplete="current-password"
                              [(ngModel)]="password" required /></label>
          @if (error()) { <p class="admin-error" role="alert">{{ error() }}</p> }
          <button class="admin-primary" type="submit" [disabled]="loginForm.invalid || loading()">Entrar</button>
          <a routerLink="/home">Ir à loja</a>
        </form>
      </main>
    </div>
  `,
})
export class AdminLogin {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly loading = signal(false);
  readonly error = signal('');
  email = '';
  password = '';

  submit(): void {
    if (this.loading()) return;
    this.error.set('');
    this.loading.set(true);
    this.auth.login({ email: this.email, password: this.password }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (response) => {
        this.loading.set(false);
        if (response.user.role !== 'ADMIN') {
          this.error.set('Acesso reservado à administração.');
          return;
        }
        this.auth.setSession(response);
        void this.router.navigate(['/admin']);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.error.set(error instanceof HttpErrorResponse && error.status === 401
          ? 'E-mail ou senha inválidos.'
          : 'Não foi possível entrar. Tente novamente.');
      },
    });
  }
}
