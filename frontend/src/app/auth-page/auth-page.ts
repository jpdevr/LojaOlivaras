import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, LoginResponse, RegisterRequest } from '../core/auth.service';
import { CartService } from '../core/cart.service';
import { StoreNavigation } from '../shared/store-navigation';

type AuthView = 'login' | 'register';

@Component({
  selector: 'app-auth-page',
  imports: [FormsModule, StoreNavigation],
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

  constructor() {
    if (this.router.currentNavigation()?.extras.state?.['passwordChanged'] === true) {
      this.successMessage.set('Senha alterada. Entre com a nova senha.');
    }
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.view.set(params.get('view') === 'register' ? 'register' : 'login');
    });
  }

  loginForm = { email: '', password: '' };
  registerForm: RegisterRequest = {
    profileName: '',
    email: '',
    password: '',
    phone: '',
    birthDate: '',
  };
  confirmRegisterPassword = '';

  selectView(view: AuthView): void {
    this.view.set(view);
    this.clearMessages();
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParamsHandling: 'merge',
      queryParams: { view: view === 'register' ? 'register' : null },
      replaceUrl: true,
    });
  }

  submitLogin(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.clearMessages();

    this.auth.login(this.loginForm).subscribe({
      next: (response: LoginResponse) => {
        this.auth.setSession(response);
        this.cart.mergeGuestCart().subscribe({
          next: ({ skippedUnits }) => {
            this.loading.set(false);
            void this.router.navigateByUrl(skippedUnits ? '/cart' : this.safeReturnUrl());
          },
          error: () => {
            this.loading.set(false);
            void this.router.navigateByUrl('/cart');
          },
        });
      },
      error: (error: unknown) => this.handleError(error),
    });
  }

  submitRegister(): void {
    if (this.loading()) return;
    this.clearMessages();
    if (this.registerForm.password !== this.confirmRegisterPassword) {
      this.errorMessage.set('As senhas não conferem.');
      return;
    }
    this.loading.set(true);

    this.auth.register(this.registerForm).subscribe({
      next: () => {
        this.loginForm.email = this.registerForm.email;
        this.loginForm.password = '';
        this.registerForm.password = '';
        this.confirmRegisterPassword = '';
        this.selectView('login');
        this.successMessage.set('Conta criada. Entre com seu e-mail e senha.');
        this.loading.set(false);
      },
      error: (error: unknown) => this.handleError(error),
    });
  }

  private handleError(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      const apiMessage = error.error?.fieldErrors?.birthDate ?? error.error?.message;
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

}
