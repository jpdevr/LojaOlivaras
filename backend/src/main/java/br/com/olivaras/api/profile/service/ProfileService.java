package br.com.olivaras.api.profile.service;

import br.com.olivaras.api.address.repository.AddressRepository;
import br.com.olivaras.api.auth.dto.UserResponse;
import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import br.com.olivaras.api.auth.service.GoogleTokenVerifier;
import br.com.olivaras.api.profile.dto.ChangePasswordRequest;
import br.com.olivaras.api.profile.dto.DeleteAccountRequest;
import br.com.olivaras.api.profile.dto.UpdateProfileRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final UserRepository users;
    private final AddressRepository addresses;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;

    public ProfileService(UserRepository users, AddressRepository addresses, PasswordEncoder passwordEncoder,
                         GoogleTokenVerifier googleTokenVerifier) {
        this.users = users;
        this.addresses = addresses;
        this.passwordEncoder = passwordEncoder;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) { return UserResponse.from(activeUser(userId)); }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        if (request.profileName() == null && request.phone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um campo para atualizar");
        }
        UserEntity user = activeUser(userId);
        user.updateProfile(request.profileName() == null ? null : request.profileName().trim(),
                request.phone() == null ? null : request.phone().trim());
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        UserEntity user = activeUser(userId);
        String currentHash = user.getPasswordHash();
        if (currentHash == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta conta usa login Google e não possui senha local para alterar");
        }
        if (!passwordEncoder.matches(request.currentPassword(), currentHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteAccount(UUID userId, DeleteAccountRequest request) {
        UserEntity user = activeUser(userId);
        if (user.getPasswordHash() != null) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()
                    || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Confirme a exclusão informando sua senha atual");
            }
        } else if (user.getGoogleSubject() != null) {
            if (request.googleIdToken() == null || request.googleIdToken().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Confirme a exclusão autenticando novamente com o Google");
            }
            GoogleTokenVerifier.GoogleIdentity identity = googleTokenVerifier.verifyFresh(
                    request.googleIdToken(), Duration.ofMinutes(5));
            if (!user.getGoogleSubject().equals(identity.subject())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "A autenticação Google não corresponde a esta conta");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Método de autenticação indisponível");
        }

        addresses.deleteAllByUser_Id(userId);
        addresses.flush();
        user.anonymizeAndDeactivate(Instant.now());
    }

    private UserEntity activeUser(UUID userId) {
        return users.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado"));
    }
}
