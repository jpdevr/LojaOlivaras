package br.com.olivaras.api.wishlist.controller;

import br.com.olivaras.api.wishlist.dto.WishlistItemRequest;
import br.com.olivaras.api.wishlist.dto.WishlistItemResponse;
import br.com.olivaras.api.wishlist.dto.WishlistResponse;
import br.com.olivaras.api.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojaolivaras/api/wishlist")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist", description = "Lista de desejos da conta autenticada")
public class WishlistController {
    private final WishlistService wishlist;

    public WishlistController(WishlistService wishlist) { this.wishlist = wishlist; }

    @GetMapping("/list")
    @Operation(summary = "Lista itens salvos na wishlist própria")
    public WishlistResponse list(@AuthenticationPrincipal Jwt jwt) {
        return wishlist.list(userId(jwt));
    }

    @PostMapping("/add")
    @Operation(summary = "Salva item do catálogo na wishlist própria; repetição não duplica")
    public WishlistItemResponse add(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody WishlistItemRequest request) {
        return wishlist.add(userId(jwt), request);
    }

    @PostMapping("/remove")
    @Operation(summary = "Remove item da wishlist própria")
    public WishlistResponse remove(@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody WishlistItemRequest request) {
        return wishlist.remove(userId(jwt), request);
    }

    @PostMapping("/clear")
    @Operation(summary = "Limpa a wishlist própria")
    public WishlistResponse clear(@AuthenticationPrincipal Jwt jwt) {
        return wishlist.clear(userId(jwt));
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
