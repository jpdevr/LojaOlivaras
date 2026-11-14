package br.com.olivaras.api.auth.dto;

import br.com.olivaras.api.auth.validation.OlderThanSixteen;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record GoogleLoginRequest(
        @jakarta.validation.constraints.NotBlank @Size(max = 8192) String idToken,
        @Size(min = 2, max = 80) String profileName,
        @Pattern(regexp = "^\\+?[0-9 ()-]{10,30}$") String phone,
        @OlderThanSixteen LocalDate birthDate) {}
