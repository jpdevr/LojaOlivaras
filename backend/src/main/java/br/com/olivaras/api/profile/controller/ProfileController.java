package br.com.olivaras.api.profile.controller;

import br.com.olivaras.api.auth.dto.UserResponse;
import br.com.olivaras.api.profile.dto.ChangePasswordRequest;
import br.com.olivaras.api.profile.dto.DeleteAccountRequest;
import br.com.olivaras.api.profile.dto.UpdateProfileRequest;
import br.com.olivaras.api.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojaolivaras/api/profile")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfil", description = "Dados e segurança da conta autenticada")
public class ProfileController {
    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) { this.profiles = profiles; }

    @GetMapping
    @Operation(summary = "Consulta o próprio perfil")
    public UserResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
        return profiles.getProfile(userId(jwt));
    }

    @PostMapping("/edit")
    @Operation(summary = "Atualiza nome de perfil e/ou telefone")
    public UserResponse updateProfile(@AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return profiles.updateProfile(userId(jwt), request);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Altera a senha após confirmar a senha atual")
    public void changePassword(@AuthenticationPrincipal Jwt jwt,
                               @Valid @RequestBody ChangePasswordRequest request) {
        profiles.changePassword(userId(jwt), request);
    }

    @PostMapping("/deleteaccount")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerra e anonimiza a própria conta após reautenticação")
    public void deleteAccount(@AuthenticationPrincipal Jwt jwt,
                              @Valid @RequestBody DeleteAccountRequest request) {
        profiles.deleteAccount(userId(jwt), request);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
