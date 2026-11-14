package br.com.olivaras.api.order.dto;

import br.com.olivaras.api.order.entity.StoreOrderEntity;
import java.time.Instant;
import java.util.UUID;

public record PaymentStatusResponse(UUID orderId, String paymentStatus, String paymentReference,
                                   Instant orderCreatedAt, String note) {
    public static PaymentStatusResponse from(StoreOrderEntity order) {
        return new PaymentStatusResponse(order.getId(), order.getPaymentStatus(), order.getPaymentReference(),
                order.getCreatedAt(), "Pagamento fictício para fins acadêmicos; nenhuma cobrança foi realizada.");
    }
}
