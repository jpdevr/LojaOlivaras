import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest extends LoginRequest {
  profileName: string;
  phone: string;
  birthDate: string;
}

export interface UserResponse {
  id: string;
  profileName: string;
  email: string;
  phone: string | null;
  birthDate: string | null;
  role: string;
  createdAt: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: UserResponse;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authUrl = 'http://localhost:8080/lojaolivaras/api/auth';
  private accessToken: string | null = null;
  private readonly currentUser = signal<UserResponse | null>(null);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login`, request);
  }

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.authUrl}/register`, request);
  }

  setSession(response: LoginResponse): void {
    this.accessToken = response.accessToken;
    this.currentUser.set(response.user);
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  getCurrentUser(): typeof this.currentUser {
    return this.currentUser;
  }

  updateCurrentUser(user: UserResponse): void {
    this.currentUser.set(user);
  }

  clearSession(): void {
    this.accessToken = null;
    this.currentUser.set(null);
  }
}
