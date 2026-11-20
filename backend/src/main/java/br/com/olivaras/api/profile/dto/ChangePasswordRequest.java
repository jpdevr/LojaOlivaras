package br.com.olivaras.api.profile.dto;

import br.com.olivaras.api.auth.validation.BcryptPasswordLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(max = 72) String currentPassword,
        @NotBlank @BcryptPasswordLength String newPassword) {}
