package br.com.olivaras.api.auth.service;

import br.com.olivaras.api.auth.dto.GoogleLoginRequest;
import br.com.olivaras.api.auth.dto.LoginRequest;
import br.com.olivaras.api.auth.dto.LoginResponse;
import br.com.olivaras.api.auth.dto.RegisterRequest;
import br.com.olivaras.api.auth.dto.UserResponse;
import br.com.olivaras.api.auth.entity.Role;
import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService tokens;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthTokenService tokens, GoogleTokenVerifier googleTokenVerifier) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateAge(request.birthDate());
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado");
        }
        UserEntity user = new UserEntity(request.profileName().trim(), email,
                passwordEncoder.encode(request.password()), null, request.phone().trim(), request.birthDate());
        return UserResponse.from(saveNewUser(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(UserEntity::isActive)
                .filter(candidate -> candidate.getRole() == Role.CUSTOMER)
                .orElseThrow(this::invalidCredentials);
        String hash = user.getPasswordHash();
        if (hash == null || !passwordEncoder.matches(request.password(), hash)) {
            throw invalidCredentials();
        }
        return tokens.issueFor(user);
    }

    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleIdentity google = googleTokenVerifier.verify(request.idToken());

        var linkedAccount = users.findByGoogleSubject(google.subject());
        if (linkedAccount.isPresent()) {
            UserEntity user = linkedAccount.get();
            if (!user.isActive() || user.getRole() != Role.CUSTOMER) throw invalidCredentials();
            return tokens.issueFor(user);
        }

        if (users.existsByEmailIgnoreCase(google.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este e-mail já possui cadastro. Entre com a senha para vincular outros métodos futuramente");
        }
        if (request.profileName() == null || request.profileName().isBlank()
                || request.phone() == null || request.phone().isBlank() || request.birthDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No primeiro acesso Google, informe nome de perfil, telefone e data de nascimento");
        }
        validateAge(request.birthDate());

        UserEntity user = new UserEntity(request.profileName().trim(), google.email(), null,
                google.subject(), request.phone().trim(), request.birthDate());
        return tokens.issueFor(saveNewUser(user));
    }

    private UserEntity saveNewUser(UserEntity user) {
        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado", ex);
        }
    }

    private void validateAge(LocalDate birthDate) {
        if (birthDate == null || !birthDate.isBefore(LocalDate.now(BUSINESS_ZONE).minusYears(16))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário ter mais de 16 anos");
        }
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }
}
