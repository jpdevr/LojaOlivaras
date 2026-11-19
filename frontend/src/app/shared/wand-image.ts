import { Component, computed, input } from '@angular/core';
import { wandArt } from './product-art';

let nextOutlineId = 0;

@Component({
  selector: 'app-wand-image',
  template: `
    @if (imageUrl(); as url) {
      <img class="wand-photo" [src]="url" [alt]="name()" loading="lazy" decoding="async">
    } @else if (art(); as visual) {
      <svg class="wand-sheet-crop" [class.is-reference]="visual.kind === 'reference'"
           [class.has-grey-matte]="visual.sheet.greyMatte"
           [attr.viewBox]="visual.crop.join(' ')" [attr.width]="visual.crop[2]" [attr.height]="visual.crop[3]"
           role="img" [attr.aria-label]="name()">
        @if (visual.outline; as outline) {
          <defs>
            <clipPath [attr.id]="outlineId">
              <path [attr.d]="outline" />
            </clipPath>
          </defs>
        }
        <image [attr.href]="visual.sheet.url" [attr.width]="visual.sheet.width"
               [attr.clip-path]="visual.outline ? 'url(#' + outlineId + ')' : null"
               [attr.height]="visual.sheet.height" preserveAspectRatio="none" />
      </svg>
    }
  `,
})
export class WandImage {
  readonly outlineId = 'wand-outline-' + nextOutlineId++;
  readonly name = input.required<string>();
  readonly imageUrl = input<string | null>(null);
  readonly art = computed(() => wandArt(this.name()));
}
