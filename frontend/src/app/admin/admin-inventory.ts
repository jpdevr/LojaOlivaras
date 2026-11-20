import { ChangeDetectorRef, Component, DestroyRef, ElementRef, effect, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { AdminApi, NewWand, StockItem, StockPage } from './admin-api';
import { apiErrorMessage } from '../core/api';

type StockEditor = { type: 'create' } | { type: 'adjust'; item: StockItem };

@Component({
  selector: 'app-admin-inventory',
  imports: [FormsModule],
  templateUrl: './admin-inventory.html',
})
export class AdminInventory {
  private readonly api = inject(AdminApi);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly editorDialog = viewChild.required<ElementRef<HTMLDialogElement>>('editorDialog');
  readonly criteria = signal({ search: '', page: 0 });
  readonly stock = signal<StockPage | null>(null);
  readonly editor = signal<StockEditor | null>(null);
  readonly loadFailed = signal(false);
  readonly saving = signal(false);
  readonly message = signal('');
  readonly error = signal('');
  readonly number = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 });
  readonly imageLabels = ['Imagem principal', 'Imagem complementar 1', 'Imagem complementar 2'];
  selectedImages: (File | null)[] = [null, null, null];
  search = '';
  quantity: number | null = null;
  direction: 'in' | 'out' = 'in';
  draft: NewWand = this.emptyWand();

  constructor() {
    effect((onCleanup) => {
      const { search, page } = this.criteria();
      this.loadFailed.set(false);
      this.stock.set(null);
      const request = this.api.stock(search, page).subscribe({
        next: (stock) => this.stock.set(stock),
        error: () => this.loadFailed.set(true),
      });
      onCleanup(() => request.unsubscribe());
    });
  }

  filter(): void {
    this.criteria.set({ search: this.search.trim(), page: 0 });
  }

  goToPage(page: number): void {
    this.criteria.update((criteria) => ({ ...criteria, page }));
  }

  beginCreate(): void {
    this.draft = this.emptyWand();
    this.selectedImages = [null, null, null];
    this.openEditor({ type: 'create' });
  }

  beginAdjustment(item: StockItem): void {
    this.quantity = null;
    this.direction = 'in';
    this.openEditor({ type: 'adjust', item });
  }

  cancel(): void {
    if (!this.saving()) this.editorDialog().nativeElement.close();
  }

  selectImage(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0) ?? null;
    this.error.set('');
    if (file && file.size > 5 * 1024 * 1024) {
      input.value = '';
      this.selectedImages[index] = null;
      this.error.set('Cada imagem pode ter até 5 MB.');
      return;
    }
    this.selectedImages[index] = file;
  }

  saveWand(): void {
    if (!Number.isInteger(this.draft.stock)) {
      this.error.set('Informe um número inteiro de unidades.');
      return;
    }
    if (!this.selectedImages[0]) {
      this.error.set('Selecione a imagem principal.');
      return;
    }
    const images = this.selectedImages.filter((image): image is File => image !== null);
    this.save(this.api.createWand({
      ...this.draft,
      name: this.draft.name.trim(),
      description: this.draft.description.trim(),
    }, images), (item) => {
      this.search = item.name;
      this.filter();
      return `Varinha cadastrada com ${this.number.format(item.stock)} unidade(s).`;
    });
  }

  adjustStock(item: StockItem): void {
    if (this.quantity === null || !Number.isInteger(this.quantity) || this.quantity <= 0) {
      this.error.set('Informe uma quantidade inteira maior que zero.');
      return;
    }
    const change = this.direction === 'out' ? -this.quantity : this.quantity;
    this.save(this.api.adjustStock(item.id, change), (updated) => {
      this.goToPage(this.criteria().page);
      return `Estoque atualizado para ${this.number.format(updated.stock)} unidade(s).`;
    });
  }

  private openEditor(editor: StockEditor): void {
    this.error.set('');
    this.message.set('');
    this.editor.set(editor);
    this.changeDetector.detectChanges();
    this.editorDialog().nativeElement.showModal();
  }

  private save(request: Observable<StockItem>, onSaved: (item: StockItem) => string): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (item) => {
        this.saving.set(false);
        this.cancel();
        this.message.set(onSaved(item));
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.error.set(apiErrorMessage(error, 'Não foi possível salvar. Tente novamente.'));
      },
    });
  }

  private emptyWand(): NewWand {
    return {
      name: '', description: '', priceGalleons: null, stock: 0, wood: '', core: '', lengthInches: null,
      flexibility: '', maker: '',
    };
  }
}
