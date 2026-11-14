package br.com.olivaras.api.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 80) @Pattern(regexp = ".*\\S.*") String profileName,
        @Pattern(regexp = "^\\+?[0-9 ()-]{10,30}$") String phone) {}
