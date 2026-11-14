package br.com.olivaras.api.cart.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RemoveCartItemRequest(@NotNull UUID itemId) {}
