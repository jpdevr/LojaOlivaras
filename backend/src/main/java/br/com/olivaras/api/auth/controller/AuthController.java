package br.com.olivaras.api.auth.controller;

import br.com.olivaras.api.auth.dto.LoginRequest;
import br.com.olivaras.api.auth.dto.LoginResponse;
import br.com.olivaras.api.auth.dto.RegisterRequest;
import br.com.olivaras.api.auth.dto.UserResponse;
import br.com.olivaras.api.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lojaolivaras/api/auth")
@Tag(name = "Autenticação", description = "Cadastro e login por senha")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    @Operation(summary = "Cria uma conta de cliente", description = "Exige e-mail, nome de perfil, senha, telefone e idade superior a 16 anos.")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica por e-mail e senha")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
