package br.com.olivaras.api.auth.service;

import java.util.Set;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class GoogleTokenVerifier {
    private final JwtDecoder decoder;
    private final String clientId;
    private final boolean enabled;

    public GoogleTokenVerifier(@Value("${app.auth.google.client-id:}") String clientId,
            @Value("${app.auth.google.enabled:false}") boolean enabled) {
        this.clientId = clientId;
        this.enabled = enabled;
        NimbusJwtDecoder googleDecoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();
        OAuth2TokenValidator<Jwt> googleClaims = token -> {
            boolean validIssuer = Set.of("https://accounts.google.com", "accounts.google.com")
                    .contains(token.getIssuer() == null ? "" : token.getIssuer().toString());
            boolean validAudience = !clientId.isBlank() && token.getAudience().contains(clientId);
            Object emailVerifiedClaim = token.getClaim("email_verified");
            boolean verifiedEmail = Boolean.TRUE.equals(emailVerifiedClaim)
                    || "true".equalsIgnoreCase(String.valueOf(emailVerifiedClaim));
            if (validIssuer && validAudience && verifiedEmail) return OAuth2TokenValidatorResult.success();
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token Google inválido", null));
        };
        googleDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), googleClaims));
        this.decoder = googleDecoder;
    }

    public GoogleIdentity verify(String idToken) {
        if (!enabled || clientId.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "Login Google ainda não foi configurado no ambiente");
        }
        try {
            Jwt jwt = decoder.decode(idToken);
            String subject = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Token Google inválido");
            }
            return new GoogleIdentity(subject, email.trim().toLowerCase(Locale.ROOT));
        } catch (JwtException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Token Google inválido", ex);
        }
    }

    public record GoogleIdentity(String subject, String email) {}
}
