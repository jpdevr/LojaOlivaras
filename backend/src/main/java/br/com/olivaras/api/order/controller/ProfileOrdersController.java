package br.com.olivaras.api.order.controller;

import br.com.olivaras.api.order.dto.OrderResponse;
import br.com.olivaras.api.order.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojaolivaras/api/profile/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Histórico de pedidos", description = "Pedidos da conta autenticada")
public class ProfileOrdersController {
    private final CheckoutService checkout;

    public ProfileOrdersController(CheckoutService checkout) { this.checkout = checkout; }

    @GetMapping({"", "/"})
    @Operation(summary = "Lista pedidos próprios em ordem do mais recente")
    public Page<OrderResponse> list(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return checkout.orderHistory(userId(jwt), page, size);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
