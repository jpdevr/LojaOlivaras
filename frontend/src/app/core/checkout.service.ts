import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type PaymentMethod = 'PIX' | 'BOLETO' | 'CREDIT_CARD';

export interface CheckoutAddress {
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
}

export interface OrderLine {
  name: string;
  wood: string | null;
  core: string | null;
  lengthInches: number | null;
  unitPriceGalleons: number;
  quantity: number;
  lineTotalGalleons: number;
}

export interface OrderDeliveryAddress {
  recipient: string;
  zipCode: string;
  street: string;
  number: string;
  complement: string | null;
  district: string;
  city: string;
  state: string;
  reference: string | null;
}

export interface OrderResponse {
  id: string;
  status: string;
  paymentStatus: string;
  paymentReference: string;
  paymentMethod: string;
  paymentInstructions: string | null;
  paymentExpiresAt: string | null;
  paidAt: string | null;
  estimatedDeliveryAt: string | null;
  deliveredAt: string | null;
  totalGalleons: number;
  createdAt: string;
  address: OrderDeliveryAddress;
  items: OrderLine[];
}

export interface PaymentStatusResponse {
  orderId: string;
  orderStatus: string;
  paymentMethod: string;
  paymentStatus: string;
  paymentReference: string;
  orderCreatedAt: string;
  paymentExpiresAt: string | null;
  paidAt: string | null;
  estimatedDeliveryAt: string | null;
  deliveredAt: string | null;
  secondsRemaining: number;
  paymentInstructions: string | null;
  qrCodeDataUri: string | null;
  note: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private readonly http = inject(HttpClient);
  private readonly checkoutUrl = 'http://localhost:8080/lojaolivaras/api/checkout';
  private readonly ordersUrl = 'http://localhost:8080/lojaolivaras/api/profile/orders';

  listAddresses(): Observable<CheckoutAddress[]> {
    return this.http.get<CheckoutAddress[]>(`${this.checkoutUrl}/addresses`);
  }

  confirm(addressId: string, paymentMethod: PaymentMethod): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.checkoutUrl}/confirm`, { addressId, paymentMethod });
  }

  getOrder(orderId: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.checkoutUrl}/order/${orderId}`);
  }

  getPaymentStatus(orderId: string): Observable<PaymentStatusResponse> {
    return this.http.get<PaymentStatusResponse>(`${this.checkoutUrl}/payment-status/${orderId}`);
  }

  listOrders(page = 0, size = 20): Observable<PageResponse<OrderResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<OrderResponse>>(this.ordersUrl, { params });
  }

  downloadBoleto(orderId: string): Observable<Blob> {
    return this.http.get(`${this.checkoutUrl}/order/${orderId}/boleto`, { responseType: 'blob' });
  }
}
