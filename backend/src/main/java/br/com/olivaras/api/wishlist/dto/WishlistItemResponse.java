package br.com.olivaras.api.wishlist.dto;

import br.com.olivaras.api.wishlist.entity.WishlistItemEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WishlistItemResponse(UUID itemId, String name, String ownerName, String wood, String core,
                                   BigDecimal lengthInches, BigDecimal priceGalleons, boolean available, int stock,
                                   String imageUrl, Instant addedAt) {
    public static WishlistItemResponse from(WishlistItemEntity entry) {
        var product = entry.getProduct();
        return new WishlistItemResponse(product.getId(), product.getName(), product.getOwnerName(),
                product.getWood(), product.getCore(), product.getLengthInches(), product.getPriceGalleons(),
                product.isActive() && product.getStock() > 0, product.isActive() ? product.getStock() : 0,
                product.getImageUrl(), entry.getCreatedAt());
    }
}
