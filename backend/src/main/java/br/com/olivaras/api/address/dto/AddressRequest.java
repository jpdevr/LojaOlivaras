package br.com.olivaras.api.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 40) String label,
        @NotBlank @Size(max = 120) String recipient,
        @NotBlank @Pattern(regexp = "^\\d{5}-?\\d{3}$") String zipCode,
        @NotBlank @Size(max = 160) String street,
        @NotBlank @Size(max = 30) String number,
        @Size(max = 120) String complement,
        @NotBlank @Size(max = 120) String district,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String state,
        @Size(max = 160) String reference,
        Boolean defaultAddress) {}
