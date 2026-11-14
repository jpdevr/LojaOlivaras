package br.com.olivaras.api.address.controller;

import br.com.olivaras.api.address.dto.AddressRequest;
import br.com.olivaras.api.address.dto.AddressResponse;
import br.com.olivaras.api.address.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/lojaolivaras/api/profile/address")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Endereços", description = "Endereços da conta autenticada")
public class AddressController {
    private final AddressService addresses;

    public AddressController(AddressService addresses) { this.addresses = addresses; }

    @GetMapping
    @Operation(summary = "Lista os próprios endereços")
    public List<AddressResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return addresses.list(userId(jwt));
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra endereço na própria conta")
    public AddressResponse create(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody AddressRequest request) {
        return addresses.create(userId(jwt), request);
    }

    @PostMapping("/edit/{addressId}")
    @Operation(summary = "Substitui um endereço próprio")
    public AddressResponse replace(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId,
                                   @Valid @RequestBody AddressRequest request) {
        return addresses.replace(userId(jwt), addressId, request);
    }

    @PostMapping("/delete/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um endereço próprio")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId) {
        addresses.delete(userId(jwt), addressId);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
