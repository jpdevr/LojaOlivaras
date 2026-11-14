package br.com.olivaras.api.wishlist.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WishlistItemRequest(@NotNull UUID itemId) {}
