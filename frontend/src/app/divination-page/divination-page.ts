import { Component, DestroyRef, ElementRef, Injector, afterNextRender, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { finalize, forkJoin, map, of, switchMap, timer } from 'rxjs';
import { CartService } from '../core/cart.service';
import { DivinationQuestion, DivinationService, PersonalWand } from '../core/divination.service';
import { StoreIcon } from '../shared/store-icon';
import { StoreNavigation } from '../shared/store-navigation';
import { WandImage } from '../shared/wand-image';
import { apiErrorMessage } from '../core/api';

@Component({
  selector: 'app-divination-page',
  imports: [RouterLink, StoreIcon, StoreNavigation, WandImage],
  templateUrl: './divination-page.html',
})
export class DivinationPage {
  private readonly api = inject(DivinationService);
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly destroyRef = inject(DestroyRef);
  private readonly title = viewChild<ElementRef<HTMLElement>>('stageTitle');
  private readonly destroyDialog = viewChild<ElementRef<HTMLDialogElement>>('destroyDialog');
  private readonly numberFormat = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
  private readonly answers = signal<number[]>([]);

  readonly questions = signal<DivinationQuestion[]>([]);
  readonly position = signal(0);
  readonly question = computed(() => this.questions()[this.position()]);
  readonly selection = computed(() => this.answers()[this.position()] ?? null);
  readonly wand = signal<PersonalWand | null>(null);
  readonly loading = signal(true);
  readonly started = signal(false);
  readonly revealing = signal(false);
  readonly justRevealed = signal(false);
  readonly adding = signal(false);
  readonly destroying = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);
  readonly destroyError = signal<string | null>(null);

  constructor() { this.load(); }

  load(focus = false): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.api.current().pipe(
      switchMap((wand) => wand
        ? of({ wand, questions: [] })
        : this.api.questions().pipe(map((questions) => ({ wand: null, questions })))),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: ({ wand, questions }) => {
        this.wand.set(wand);
        this.questions.set(questions);
        if (focus) this.focusStage();
      },
      error: () => this.loadError.set('Não foi possível abrir a Adivinhação. Tente novamente.'),
    });
  }

  start(): void {
    this.started.set(true);
    this.focusStage();
  }

  choose(option: number): void {
    const answers = [...this.answers()];
    answers[this.position()] = option;
    this.answers.set(answers);
  }

  back(): void {
    if (this.position() > 0) this.position.update((value) => value - 1);
    else this.started.set(false);
    this.actionError.set(null);
    this.focusStage();
  }

  next(): void {
    if (this.selection() === null || this.revealing()) return;
    if (this.position() + 1 < this.questions().length) {
      this.position.update((value) => value + 1);
      this.focusStage();
      return;
    }

    this.actionError.set(null);
    this.revealing.set(true);
    this.focusStage();
    const delay = matchMedia('(prefers-reduced-motion: reduce)').matches ? of(0) : timer(1750);
    forkJoin({ wand: this.api.reveal(this.answers()), delay }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.revealing.set(false)),
    ).subscribe({
      next: ({ wand }) => {
        this.wand.set(wand);
        this.justRevealed.set(true);
        this.focusStage();
      },
      error: (error: unknown) => {
        this.actionError.set(apiErrorMessage(error, 'Não foi possível revelar sua varinha. Tente novamente.'));
        this.focusStage();
      },
    });
  }

  addToCart(wand: PersonalWand): void {
    if (!wand.available || this.adding() || this.destroying()) return;
    this.adding.set(true);
    this.actionError.set(null);
    this.cart.getCart().pipe(
      switchMap((cart) => cart.items.some((item) => item.itemId === wand.id)
        ? of(cart)
        : this.cart.add(wand.id)),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.adding.set(false)),
    ).subscribe({
      next: () => void this.router.navigate(['/cart']),
      error: (error: unknown) => this.actionError.set(apiErrorMessage(error, 'Não foi possível levar sua varinha ao carrinho.')),
    });
  }

  askToDestroy(): void {
    if (this.adding() || this.destroying()) return;
    this.destroyError.set(null);
    this.destroyDialog()?.nativeElement.showModal();
  }

  cancelDestruction(event?: Event): void {
    if (this.destroying()) {
      event?.preventDefault();
    } else {
      this.destroyDialog()?.nativeElement.close();
    }
  }

  destroyWand(wandId: string): void {
    if (this.destroying()) return;
    this.destroyError.set(null);
    this.destroying.set(true);
    this.api.destroy(wandId).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.destroying.set(false)),
    ).subscribe({
      next: () => {
        this.destroyDialog()?.nativeElement.close();
        this.cart.refreshTotal();
        this.wand.set(null);
        this.started.set(false);
        this.answers.set([]);
        this.position.set(0);
        this.justRevealed.set(false);
        this.actionError.set(null);
        this.load(true);
      },
      error: (error: unknown) => this.destroyError.set(apiErrorMessage(error, 'Não foi possível destruir a varinha. Tente novamente.')),
    });
  }

  formatLength(inches: number): string { return this.numberFormat.format(inches); }

  private focusStage(): void {
    afterNextRender(() => this.title()?.nativeElement.focus(), { injector: this.injector });
  }
}
