package br.com.olivaras.api.order.dto;

import br.com.olivaras.api.order.entity.StoreOrderEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, String status, String paymentStatus, String paymentReference,
                            String paymentMethod, String paymentInstructions, Instant paymentExpiresAt,
                            Instant paidAt, Instant estimatedDeliveryAt, Instant deliveredAt,
                            BigDecimal totalGalleons, Instant createdAt, OrderAddressResponse address,
                            List<OrderItemResponse> items) {
    public static OrderResponse from(StoreOrderEntity order, List<OrderItemResponse> items) {
        return new OrderResponse(order.getId(), order.getStatus(), order.getPaymentStatus(),
                order.getPaymentReference(), order.getPaymentMethod(), order.getPaymentInstructions(),
                order.getPaymentExpiresAt(), order.getPaidAt(), order.getEstimatedDeliveryAt(),
                order.getDeliveredAt(), order.getTotalGalleons(), order.getCreatedAt(),
                OrderAddressResponse.from(order), items);
    }
}
