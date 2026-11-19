package br.com.olivaras.api.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record GuestCartTransferRequest(
        @NotNull UUID transferId,
        @NotEmpty List<@NotNull @Valid AddCartItemRequest> items) {}
