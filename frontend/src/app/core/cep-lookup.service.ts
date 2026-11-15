import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface CepLookupResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean | string;
}

@Injectable({ providedIn: 'root' })
export class CepLookupService {
  private readonly http = inject(HttpClient);

  lookup(cep: string): Observable<CepLookupResponse> {
    return this.http.get<CepLookupResponse>(`https://viacep.com.br/ws/${cep}/json/`);
  }
}
