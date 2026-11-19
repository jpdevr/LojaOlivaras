import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class WandDetailsService {
  readonly itemId = signal<string | null>(null);
}
