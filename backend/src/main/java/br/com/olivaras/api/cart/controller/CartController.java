package br.com.olivaras.api.cart.controller;

import br.com.olivaras.api.cart.dto.AddCartItemRequest;
import br.com.olivaras.api.cart.dto.CartResponse;
import br.com.olivaras.api.cart.dto.RemoveCartItemRequest;
import br.com.olivaras.api.cart.dto.UpdateCartQuantityRequest;
import br.com.olivaras.api.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojaolivaras/api/cart")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Carrinho", description = "Carrinho persistente da conta autenticada")
public class CartController {
    private final CartService carts;

    public CartController(CartService carts) { this.carts = carts; }

    @GetMapping("/list")
    @Operation(summary = "Consulta itens e total do próprio carrinho")
    public CartResponse list(@AuthenticationPrincipal Jwt jwt) { return carts.get(userId(jwt)); }

    @PostMapping("/add")
    @Operation(summary = "Adiciona item ou soma quantidade no carrinho")
    public CartResponse add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddCartItemRequest request) {
        return carts.add(userId(jwt), request);
    }

    @PostMapping("/remove")
    @Operation(summary = "Remove um produto do carrinho")
    public CartResponse remove(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RemoveCartItemRequest request) {
        return carts.remove(userId(jwt), request);
    }

    @PostMapping("/update-quantity/{itemId}")
    @Operation(summary = "Substitui a quantidade de um produto no carrinho")
    public CartResponse updateQuantity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID itemId,
                                       @Valid @RequestBody UpdateCartQuantityRequest request) {
        return carts.updateQuantity(userId(jwt), itemId, request);
    }

    @PostMapping("/clear")
    @Operation(summary = "Esvazia o próprio carrinho")
    public CartResponse clear(@AuthenticationPrincipal Jwt jwt) { return carts.clear(userId(jwt)); }

    @GetMapping("/total")
    @Operation(summary = "Consulta o total atual em galeões")
    public CartTotalResponse total(@AuthenticationPrincipal Jwt jwt) {
        CartResponse cart = carts.get(userId(jwt));
        return new CartTotalResponse(cart.totalGalleons(), cart.totalItems());
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }

    public record CartTotalResponse(BigDecimal totalGalleons, int totalItems) {}
}
