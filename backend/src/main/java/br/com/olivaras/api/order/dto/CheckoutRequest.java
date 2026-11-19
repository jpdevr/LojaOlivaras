package br.com.olivaras.api.order.dto;

import br.com.olivaras.api.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CheckoutRequest(
        @NotNull UUID addressId,
        @NotNull PaymentMethod paymentMethod,
        List<@NotNull UUID> itemIds) {}
