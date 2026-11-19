import { Component, input } from '@angular/core';

@Component({
  selector: 'app-wand-stock',
  template: `
    @if (stock() === 0) {
      <span class="wand-stock stock-empty">Esgotada</span>
    } @else if (stock() <= 2) {
      <span class="wand-stock stock-critical">
        <span aria-hidden="true">✦</span>
        {{ stock() === 1 ? 'Última unidade!' : 'Só 2 unidades!' }}
      </span>
    } @else if (stock() < 5) {
      <span class="wand-stock stock-low">Últimas unidades · {{ stock() }} em estoque</span>
    } @else {
      <span class="wand-stock">{{ stock() }} unidades em estoque</span>
    }
  `,
})
export class WandStock {
  readonly stock = input.required<number>();
}
