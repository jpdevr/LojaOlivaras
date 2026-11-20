package br.com.olivaras.api.order.dto;

import br.com.olivaras.api.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.hibernate.validator.constraints.br.CPF;

public record CheckoutRequest(
        @NotNull UUID addressId,
        @NotNull PaymentMethod paymentMethod,
        @NotBlank(message = "Informe o CPF") @Size(max = 14) @CPF(message = "Informe um CPF válido") String cpf,
        List<@NotNull UUID> itemIds) {}
