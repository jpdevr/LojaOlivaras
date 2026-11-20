import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { CatalogItem } from './catalog.service';
import { API_BASE_URL } from './api';

export interface DivinationQuestion {
  prompt: string;
  options: string[];
}

export type PersonalWand = Omit<CatalogItem, 'sourceReference'>;

@Injectable({ providedIn: 'root' })
export class DivinationService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/profile/wand-divination`;

  questions() { return this.http.get<DivinationQuestion[]>(`${this.url}/questions`); }

  current() { return this.http.get<PersonalWand | null>(this.url); }

  reveal(answers: number[]) { return this.http.post<PersonalWand>(this.url, { answers }); }

  destroy(wandId: string) { return this.http.delete<void>(`${this.url}/${wandId}`); }
}
