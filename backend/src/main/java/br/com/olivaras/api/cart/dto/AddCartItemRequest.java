package br.com.olivaras.api.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddCartItemRequest(@NotNull UUID itemId,
                                 @NotNull @Min(1) @Max(99) Integer quantity) {}
