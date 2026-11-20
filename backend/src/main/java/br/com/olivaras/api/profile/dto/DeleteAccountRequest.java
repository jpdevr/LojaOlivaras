package br.com.olivaras.api.profile.dto;

import jakarta.validation.constraints.Size;

public record DeleteAccountRequest(@Size(max = 72) String currentPassword) {}
