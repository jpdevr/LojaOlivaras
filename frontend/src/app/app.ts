import { DOCUMENT } from '@angular/common';
import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
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
  private readonly document = inject(DOCUMENT);
  readonly adminMode = signal(/^\/admin(?:\/|$)/.test(this.document.location.pathname));

  constructor() {
    const router = inject(Router);
    const details = inject(WandDetailsService);
    const document = this.document;
    this.applyDocumentMode(this.adminMode());

    router.events.pipe(takeUntilDestroyed()).subscribe((event) => {
      if (event instanceof NavigationStart) details.itemId.set(null);
      if (!(event instanceof NavigationEnd)) return;
      const wasAdmin = this.adminMode();
      const isAdmin = router.routerState.snapshot.root.firstChild?.routeConfig?.path === 'admin';
      this.adminMode.set(isAdmin);
      this.applyDocumentMode(isAdmin);
      if (isAdmin || wasAdmin || !router.lastSuccessfulNavigation()?.previousNavigation
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

  private applyDocumentMode(isAdmin: boolean): void {
    const root = this.document.documentElement;
    root.classList.toggle('admin-mode', isAdmin);
    if (isAdmin) root.removeAttribute('data-house-theme');
  }
}
