package br.com.olivaras.api.cart.dto;

import br.com.olivaras.api.cart.entity.CartItemEntity;
import java.math.BigDecimal;
import java.util.UUID;

public record CartLineResponse(UUID itemId, String name, int quantity, BigDecimal unitPriceGalleons,
                               BigDecimal lineTotalGalleons, boolean available, String imageUrl) {
    public static CartLineResponse from(CartItemEntity item) {
        var product = item.getProduct();
        BigDecimal lineTotal = product.getPriceGalleons().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartLineResponse(product.getId(), product.getName(), item.getQuantity(),
                product.getPriceGalleons(), lineTotal,
                product.isActive() && product.getStock() >= item.getQuantity(), product.getImageUrl());
    }
}
