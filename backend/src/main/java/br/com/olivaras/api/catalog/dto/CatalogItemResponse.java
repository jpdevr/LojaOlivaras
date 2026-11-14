package br.com.olivaras.api.catalog.dto;

import br.com.olivaras.api.catalog.entity.WandProductEntity;
import java.math.BigDecimal;
import java.util.UUID;

public record CatalogItemResponse(
        UUID id,
        String slug,
        String name,
        String ownerName,
        String wood,
        String core,
        BigDecimal lengthInches,
        String flexibility,
        String maker,
        String description,
        String canonTier,
        String sourceReference,
        BigDecimal priceGalleons,
        boolean available,
        String imageUrl) {

    public static CatalogItemResponse from(WandProductEntity item) {
        return new CatalogItemResponse(item.getId(), item.getSlug(), item.getName(), item.getOwnerName(),
                item.getWood(), item.getCore(), item.getLengthInches(), item.getFlexibility(), item.getMaker(),
                item.getDescription(), item.getCanonTier(), item.getSourceReference(), item.getPriceGalleons(),
                item.getStock() > 0, item.getImageUrl());
    }
}
