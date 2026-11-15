import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface CatalogItem {
  id: string;
  slug: string;
  name: string;
  ownerName: string | null;
  wood: string | null;
  core: string | null;
  lengthInches: number | null;
  flexibility: string | null;
  maker: string | null;
  description: string;
  canonTier: string;
  sourceReference: string;
  priceGalleons: number;
  available: boolean;
  imageUrl: string | null;
}

export interface CatalogPage {
  content: CatalogItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly catalogUrl = 'http://localhost:8080/lojaolivaras/api/catalogo';

  list(page = 0, size = 100): Observable<CatalogPage> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<CatalogPage>(`${this.catalogUrl}/list-item`, { params });
  }

  search(query: string, page = 0, size = 100): Observable<CatalogPage> {
    const params = new HttpParams().set('q', query).set('page', page).set('size', size);
    return this.http.get<CatalogPage>(`${this.catalogUrl}/search`, { params });
  }
}
