package br.com.olivaras.api.auth.service;

import br.com.olivaras.api.auth.dto.LoginResponse;
import br.com.olivaras.api.auth.dto.UserResponse;
import br.com.olivaras.api.auth.entity.UserEntity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {
    private final JwtEncoder encoder;
    private final long expirationMinutes;

    public AuthTokenService(JwtEncoder encoder,
            @Value("${app.security.jwt-expiration-minutes}") long expirationMinutes) {
        this.encoder = encoder;
        this.expirationMinutes = expirationMinutes;
    }

    public LoginResponse issueFor(UserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("olivaras-api")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("role", user.getRole().name())
                .claim("profileName", user.getProfileName())
                .claim("ver", user.getTokenVersion())
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", expirationMinutes * 60, UserResponse.from(user));
    }
}
