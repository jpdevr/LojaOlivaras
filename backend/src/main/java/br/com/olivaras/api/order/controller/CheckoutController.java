package br.com.olivaras.api.order.controller;

import br.com.olivaras.api.address.dto.AddressResponse;
import br.com.olivaras.api.address.service.AddressService;
import br.com.olivaras.api.order.dto.CheckoutRequest;
import br.com.olivaras.api.order.dto.OrderResponse;
import br.com.olivaras.api.order.dto.PaymentStatusResponse;
import br.com.olivaras.api.order.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojaolivaras/api/checkout")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Checkout e pedidos", description = "Finalização fictícia e consulta dos próprios pedidos")
public class CheckoutController {
    private final CheckoutService checkout;
    private final AddressService addresses;

    public CheckoutController(CheckoutService checkout, AddressService addresses) {
        this.checkout = checkout;
        this.addresses = addresses;
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Confirma o pedido e inicia a janela de pagamento simulado")
    public OrderResponse confirm(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CheckoutRequest request) {
        return checkout.confirm(userId(jwt), request);
    }

    @GetMapping("/addresses")
    @Operation(summary = "Lista endereços próprios disponíveis para entrega")
    public List<AddressResponse> addresses(@AuthenticationPrincipal Jwt jwt) {
        return addresses.list(userId(jwt));
    }

    @GetMapping("/order/{id}")
    @Operation(summary = "Consulta um pedido da conta autenticada")
    public OrderResponse order(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return checkout.order(userId(jwt), id);
    }

    @GetMapping("/payment-status/{orderId}")
    @Operation(summary = "Consulta o estado do pagamento simulado de um pedido próprio")
    public PaymentStatusResponse paymentStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return checkout.paymentStatus(userId(jwt), orderId);
    }

    @GetMapping(value = "/order/{id}/boleto", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Baixa o boleto ficticio em PDF de um pedido proprio")
    public ResponseEntity<byte[]> boleto(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        byte[] pdf = checkout.boletoPdf(userId(jwt), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("boleto-simulado-" + id + ".pdf").build());
        headers.setCacheControl("no-store");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
