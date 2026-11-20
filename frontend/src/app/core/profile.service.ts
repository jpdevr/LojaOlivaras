import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { UserResponse } from './auth.service';
import { API_BASE_URL } from './api';

export interface ProfileUpdate {
  profileName: string;
  phone: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface Address {
  id: string;
  label: string | null;
  recipient: string;
  zipCode: string;
  street: string;
  number: string;
  complement: string | null;
  district: string;
  city: string;
  state: string;
  reference: string | null;
  defaultAddress: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AddressRequest {
  label: string | null;
  recipient: string;
  zipCode: string;
  street: string;
  number: string;
  complement: string | null;
  district: string;
  city: string;
  state: string;
  reference: string | null;
  defaultAddress: boolean | null;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly profileUrl = `${API_BASE_URL}/profile`;
  private readonly addressUrl = `${this.profileUrl}/address`;

  getProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(this.profileUrl);
  }

  updateProfile(request: ProfileUpdate): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.profileUrl}/edit`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.profileUrl}/change-password`, request);
  }

  listAddresses(): Observable<Address[]> {
    return this.http.get<Address[]>(this.addressUrl);
  }

  addAddress(request: AddressRequest): Observable<Address> {
    return this.http.post<Address>(`${this.addressUrl}/add`, request);
  }

  editAddress(id: string, request: AddressRequest): Observable<Address> {
    return this.http.post<Address>(`${this.addressUrl}/edit/${id}`, request);
  }

  deleteAddress(id: string): Observable<void> {
    return this.http.post<void>(`${this.addressUrl}/delete/${id}`, {});
  }

  makeDefaultAddress(id: string): Observable<Address> {
    return this.http.post<Address>(`${this.addressUrl}/default/${id}`, {});
  }
}
