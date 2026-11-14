package br.com.olivaras.api.auth.dto;

import br.com.olivaras.api.auth.validation.OlderThanSixteen;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 80) String profileName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 10, max = 72) String password,
        @NotBlank @Pattern(regexp = "^\\+?[0-9 ()-]{10,30}$") String phone,
        @NotNull @OlderThanSixteen LocalDate birthDate) {}
