package br.com.olivaras.api.order.dto;

import br.com.olivaras.api.order.entity.StoreOrderEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record PaymentStatusResponse(
        UUID orderId,
        String orderStatus,
        String paymentMethod,
        String paymentStatus,
        String paymentReference,
        Instant orderCreatedAt,
        Instant paymentExpiresAt,
        Instant paidAt,
        Instant estimatedDeliveryAt,
        Instant deliveredAt,
        long secondsRemaining,
        String paymentInstructions,
        String qrCodeDataUri,
        String note) {

    public static PaymentStatusResponse from(StoreOrderEntity order, String qrCodeDataUri, Instant now) {
        long secondsRemaining = "PENDING".equals(order.getPaymentStatus()) && order.getPaymentExpiresAt() != null
                ? Math.max(0, Duration.between(now, order.getPaymentExpiresAt()).toSeconds())
                : 0;
        return new PaymentStatusResponse(order.getId(), order.getStatus(), order.getPaymentMethod(),
                order.getPaymentStatus(), order.getPaymentReference(), order.getCreatedAt(),
                order.getPaymentExpiresAt(), order.getPaidAt(), order.getEstimatedDeliveryAt(),
                order.getDeliveredAt(), secondsRemaining, order.getPaymentInstructions(), qrCodeDataUri,
                "Pagamento e entrega simulados para fins acadêmicos; nenhuma cobrança real foi realizada.");
    }
}
