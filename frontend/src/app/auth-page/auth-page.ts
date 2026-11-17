import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, LoginResponse, RegisterRequest } from '../core/auth.service';
import { CartService } from '../core/cart.service';

type AuthView = 'login' | 'register';

@Component({
  selector: 'app-auth-page',
  imports: [FormsModule],
  templateUrl: './auth-page.html',
})
export class AuthPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly cart = inject(CartService);

  readonly view = signal<AuthView>('login');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly latestAllowedBirthDate = this.getLatestAllowedBirthDate();

  constructor() {
    if (this.route.snapshot.queryParamMap.get('view') === 'register') this.view.set('register');
  }

  loginForm = { email: '', password: '' };
  registerForm: RegisterRequest = {
    profileName: '',
    email: '',
    password: '',
    phone: '',
    birthDate: '',
  };

  selectView(view: AuthView): void {
    this.view.set(view);
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  submitLogin(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.clearMessages();

    this.auth.login(this.loginForm).subscribe({
      next: (response: LoginResponse) => {
        this.auth.setSession(response);
        this.cart.mergeGuestCart().subscribe({
          next: () => {
            this.loading.set(false);
            void this.router.navigateByUrl(this.safeReturnUrl());
          },
          error: () => {
            this.loading.set(false);
            this.errorMessage.set('Você entrou, mas não foi possível transferir os itens do carrinho. Tente novamente.');
          },
        });
      },
      error: (error: unknown) => this.handleError(error),
    });
  }

  submitRegister(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.clearMessages();

    this.auth.register(this.registerForm).subscribe({
      next: () => {
        this.loginForm.email = this.registerForm.email;
        this.loginForm.password = '';
        this.view.set('login');
        this.successMessage.set('Conta criada. Entre com seu e-mail e senha.');
        this.loading.set(false);
      },
      error: (error: unknown) => this.handleError(error),
    });
  }

  private handleError(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      const apiMessage = error.error?.message;
      this.errorMessage.set(typeof apiMessage === 'string'
        ? apiMessage
        : 'Não foi possível concluir a solicitação. Confira os dados e tente novamente.');
    } else {
      this.errorMessage.set('Ocorreu um erro inesperado. Tente novamente.');
    }
    this.loading.set(false);
  }

  private clearMessages(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  private safeReturnUrl(): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    return returnUrl?.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : '/home';
  }

  private getLatestAllowedBirthDate(): string {
    const latest = new Date();
    latest.setFullYear(latest.getFullYear() - 16);
    latest.setDate(latest.getDate() - 1);
    const year = latest.getFullYear();
    const month = String(latest.getMonth() + 1).padStart(2, '0');
    const day = String(latest.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
