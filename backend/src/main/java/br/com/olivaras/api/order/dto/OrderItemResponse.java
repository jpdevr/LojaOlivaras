package br.com.olivaras.api.order.dto;

import br.com.olivaras.api.order.entity.OrderItemEntity;
import java.math.BigDecimal;

public record OrderItemResponse(String name, String wood, String core, BigDecimal lengthInches,
                                BigDecimal unitPriceGalleons, int quantity, BigDecimal lineTotalGalleons) {
    public static OrderItemResponse from(OrderItemEntity item) {
        return new OrderItemResponse(item.getProductName(), item.getWood(), item.getCore(), item.getLengthInches(),
                item.getUnitPriceGalleons(), item.getQuantity(), item.getLineTotalGalleons());
    }
}
