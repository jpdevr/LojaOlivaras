package br.com.olivaras.api.auth.service;

import br.com.olivaras.api.auth.dto.LoginRequest;
import br.com.olivaras.api.auth.dto.LoginResponse;
import br.com.olivaras.api.auth.dto.RegisterRequest;
import br.com.olivaras.api.auth.dto.UserResponse;
import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, AuthTokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado");
        }
        UserEntity user = new UserEntity(request.profileName().trim(), email,
                passwordEncoder.encode(request.password()), request.phone().trim(), request.birthDate());
        return UserResponse.from(saveNewUser(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(UserEntity::isActive)
                .orElseThrow(this::invalidCredentials);
        String hash = user.getPasswordHash();
        if (hash == null || !passwordEncoder.matches(request.password(), hash)) {
            throw invalidCredentials();
        }
        return tokens.issueFor(user);
    }

    private UserEntity saveNewUser(UserEntity user) {
        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado", ex);
        }
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }
}
