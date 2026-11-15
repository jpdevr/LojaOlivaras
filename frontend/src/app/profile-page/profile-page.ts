import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Address, AddressRequest, ProfileService } from '../core/profile.service';
import { AuthService } from '../core/auth.service';
import { CepLookupService } from '../core/cep-lookup.service';
import { StoreNavigation } from '../shared/store-navigation';

function emptyAddress(): AddressRequest {
  return {
    label: '', recipient: '', zipCode: '', street: '', number: '', complement: '',
    district: '', city: '', state: '', reference: '', defaultAddress: false,
  };
}

@Component({
  selector: 'app-profile-page',
  imports: [FormsModule, RouterLink, StoreNavigation],
  templateUrl: './profile-page.html',
})
export class ProfilePage {
  private readonly profile = inject(ProfileService);
  private readonly auth = inject(AuthService);
  private readonly cepLookup = inject(CepLookupService);
  private readonly destroyRef = inject(DestroyRef);

  readonly user = this.auth.getCurrentUser();
  readonly loadingProfile = signal(true);
  readonly savingProfile = signal(false);
  readonly changingPassword = signal(false);
  readonly loadingAddresses = signal(true);
  readonly savingAddress = signal(false);
  readonly lookingUpCep = signal(false);
  readonly cepMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly addressError = signal<string | null>(null);
  readonly addresses = signal<Address[]>([]);
  readonly editingAddressId = signal<string | null>(null);
  readonly pendingAddressDeletes = signal<ReadonlySet<string>>(new Set());

  profileForm = { profileName: '', phone: '' };
  passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  addressForm: AddressRequest = emptyAddress();

  constructor() {
    this.loadProfile();
    this.loadAddresses();
  }

  loadProfile(): void {
    this.loadingProfile.set(true);
    this.profile.getProfile().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (user) => {
        this.auth.updateCurrentUser(user);
        this.profileForm = { profileName: user.profileName, phone: user.phone ?? '' };
        this.loadingProfile.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível carregar seu perfil.'));
        this.loadingProfile.set(false);
      },
    });
  }

  saveProfile(): void {
    if (this.savingProfile()) return;
    this.savingProfile.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.profile.updateProfile({
      profileName: this.profileForm.profileName.trim(),
      phone: this.profileForm.phone.trim(),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (user) => {
        this.auth.updateCurrentUser(user);
        this.profileForm = { profileName: user.profileName, phone: user.phone ?? '' };
        this.successMessage.set('Dados do perfil atualizados.');
        this.savingProfile.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível atualizar o perfil.'));
        this.savingProfile.set(false);
      },
    });
  }

  changePassword(): void {
    if (this.changingPassword()) return;
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.errorMessage.set('A confirmação da nova senha não confere.');
      return;
    }
    this.changingPassword.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.profile.changePassword({
      currentPassword: this.passwordForm.currentPassword,
      newPassword: this.passwordForm.newPassword,
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        this.successMessage.set('Senha alterada com sucesso.');
        this.changingPassword.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.errorText(error, 'Não foi possível alterar a senha.'));
        this.changingPassword.set(false);
      },
    });
  }

  loadAddresses(): void {
    this.loadingAddresses.set(true);
    this.addressError.set(null);
    this.profile.listAddresses().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (addresses) => {
        this.addresses.set(addresses);
        this.loadingAddresses.set(false);
      },
      error: (error: unknown) => {
        this.addressError.set(this.errorText(error, 'Não foi possível carregar os endereços.'));
        this.loadingAddresses.set(false);
      },
    });
  }

  editAddress(address: Address): void {
    this.editingAddressId.set(address.id);
    this.addressForm = {
      label: address.label ?? '',
      recipient: address.recipient,
      zipCode: address.zipCode,
      street: address.street,
      number: address.number,
      complement: address.complement ?? '',
      district: address.district,
      city: address.city,
      state: address.state,
      reference: address.reference ?? '',
      defaultAddress: address.defaultAddress,
    };
    this.addressError.set(null);
  }

  cancelAddressEdit(): void {
    this.editingAddressId.set(null);
    this.addressForm = emptyAddress();
    this.addressError.set(null);
  }

  isCepLookupDisabled(): boolean {
    return this.lookingUpCep() || this.addressForm.zipCode.replace(/\D/g, '').length !== 8;
  }

  lookupCep(): void {
    const cep = this.addressForm.zipCode.replace(/\D/g, '');
    this.addressError.set(null);
    this.cepMessage.set(null);

    if (cep.length !== 8) {
      this.addressError.set('Informe um CEP válido com oito dígitos.');
      return;
    }

    this.lookingUpCep.set(true);
    this.cepLookup.lookup(cep).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (result) => {
        this.lookingUpCep.set(false);
        if (result.erro === true || result.erro === 'true') {
          this.addressError.set('CEP não encontrado. Confira o número e tente novamente.');
          return;
        }

        this.addressForm = {
          ...this.addressForm,
          zipCode: result.cep?.replace(/\D/g, '') || cep,
          street: result.logradouro || this.addressForm.street,
          complement: this.addressForm.complement || result.complemento || '',
          district: result.bairro || this.addressForm.district,
          city: result.localidade || this.addressForm.city,
          state: result.uf || this.addressForm.state,
        };
        this.cepMessage.set('CEP encontrado. Confira os dados e complete número e destinatário.');
      },
      error: () => {
        this.lookingUpCep.set(false);
        this.addressError.set('Não foi possível consultar o CEP agora. Verifique o número ou tente novamente mais tarde.');
      },
    });
  }

  saveAddress(): void {
    if (this.savingAddress()) return;
    this.savingAddress.set(true);
    this.addressError.set(null);
    const editingId = this.editingAddressId();
    const request: AddressRequest = {
      ...this.addressForm,
      label: this.addressForm.label?.trim() || null,
      zipCode: this.addressForm.zipCode.replace(/\D/g, ''),
      complement: this.addressForm.complement?.trim() || null,
      reference: this.addressForm.reference?.trim() || null,
    };
    const operation = editingId
      ? this.profile.editAddress(editingId, request)
      : this.profile.addAddress(request);

    operation.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (address) => {
        this.addresses.update((addresses) => editingId
          ? addresses.map((saved) => saved.id === address.id ? address : saved)
          : [...addresses, address]);
        this.cancelAddressEdit();
        this.savingAddress.set(false);
      },
      error: (error: unknown) => {
        this.addressError.set(this.errorText(error, 'Não foi possível salvar o endereço.'));
        this.savingAddress.set(false);
      },
    });
  }

  deleteAddress(address: Address): void {
    if (this.pendingAddressDeletes().has(address.id)) return;
    this.pendingAddressDeletes.update((ids) => new Set(ids).add(address.id));
    this.addressError.set(null);
    this.profile.deleteAddress(address.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.addresses.update((addresses) => addresses.filter((saved) => saved.id !== address.id));
        if (this.editingAddressId() === address.id) this.cancelAddressEdit();
        this.pendingAddressDeletes.update((ids) => {
          const next = new Set(ids);
          next.delete(address.id);
          return next;
        });
      },
      error: (error: unknown) => {
        this.addressError.set(this.errorText(error, 'Não foi possível remover o endereço.'));
        this.pendingAddressDeletes.update((ids) => {
          const next = new Set(ids);
          next.delete(address.id);
          return next;
        });
      },
    });
  }

  private errorText(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const message = error.error?.message;
      return typeof message === 'string' ? message : fallback;
    }
    return fallback;
  }
}
