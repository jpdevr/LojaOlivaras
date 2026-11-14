package br.com.olivaras.api.config;

import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

final class ActiveAccountFilter extends OncePerRequestFilter {
    private final UserRepository users;

    ActiveAccountFilter(UserRepository users) { this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Number versionClaim = jwt.getClaim("ver");
            int tokenVersion = versionClaim == null ? 0 : versionClaim.intValue();
            UserEntity user = users.findById(UUID.fromString(jwt.getSubject())).orElse(null);
            if (user == null || !user.isActive() || user.getTokenVersion() != tokenVersion) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão inválida ou conta inativa");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
