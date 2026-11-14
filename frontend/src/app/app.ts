import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService, LoginResponse, RegisterRequest, UserResponse } from './core/auth.service';

type AuthView = 'login' | 'register';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
})
export class App {
  private readonly auth = inject(AuthService);

  readonly view = signal<AuthView>('login');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly loggedInUser = signal<UserResponse | null>(null);
  readonly latestAllowedBirthDate = this.getLatestAllowedBirthDate();

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
    this.loggedInUser.set(null);
  }

  submitLogin(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.clearMessages();

    this.auth.login(this.loginForm).subscribe({
      next: (response: LoginResponse) => {
        this.auth.setAccessToken(response.accessToken);
        this.loggedInUser.set(response.user);
        this.successMessage.set(`Login efetuado. Bem-vindo, ${response.user.profileName}.`);
        this.loading.set(false);
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
