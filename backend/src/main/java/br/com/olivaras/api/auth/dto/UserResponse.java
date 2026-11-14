package br.com.olivaras.api.auth.dto;

import br.com.olivaras.api.auth.entity.UserEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(UUID id, String profileName, String email, String phone,
                           LocalDate birthDate, String role, Instant createdAt) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getProfileName(), user.getEmail(), user.getPhone(),
                user.getBirthDate(), user.getRole().name(), user.getCreatedAt());
    }
}
