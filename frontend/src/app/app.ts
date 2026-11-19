import { DOCUMENT } from '@angular/common';
import { Component, ElementRef, inject, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { WandDetailsService } from './core/wand-details.service';
import { WandDetailsDialog } from './shared/wand-details-dialog';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, WandDetailsDialog],
  templateUrl: './app.html',
})
export class App {
  private readonly pageVeil = viewChild<ElementRef<HTMLElement>>('pageVeil');

  constructor() {
    const router = inject(Router);
    const details = inject(WandDetailsService);
    const document = inject(DOCUMENT);

    router.events.pipe(takeUntilDestroyed()).subscribe((event) => {
      if (event instanceof NavigationStart) details.itemId.set(null);
      if (!(event instanceof NavigationEnd) || !router.lastSuccessfulNavigation()?.previousNavigation
          || typeof document.startViewTransition === 'function'
          || document.defaultView?.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

      const veil = this.pageVeil()?.nativeElement;
      if (!veil) return;
      veil.getAnimations().forEach((animation) => animation.cancel());
      veil.animate([
        { opacity: .97, backgroundPosition: 'center, -110px 45%, center' },
        { opacity: .85, offset: .28 },
        { opacity: 0, backgroundPosition: 'center, 120px 55%, center' },
      ], { duration: 550, easing: 'ease-out' });
    });
  }
}
