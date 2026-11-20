package br.com.olivaras.api.admin;

import br.com.olivaras.api.catalog.entity.WandProductEntity;
import br.com.olivaras.api.catalog.repository.WandProductRepository;
import br.com.olivaras.api.catalog.service.CatalogImageStorage;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminInventoryService {
    private final WandProductRepository products;
    private final CatalogImageStorage images;

    public AdminInventoryService(WandProductRepository products, CatalogImageStorage images) {
        this.products = products;
        this.images = images;
    }

    @Transactional(readOnly = true)
    public InventoryPage list(String search, int page) {
        var result = products.findByCustomerOwnerIdIsNullAndNameContainingIgnoreCase(search.strip(),
                PageRequest.of(page, 20, Sort.by("name", "id")));
        return new InventoryPage(result.map(InventoryItem::from).getContent(), result.getTotalPages(),
                result.getTotalElements());
    }

    @Transactional
    public InventoryItem create(NewWand request, List<MultipartFile> files) {
        WandProductEntity product = WandProductEntity.forSale(request.name().strip(), optional(request.wood()),
                optional(request.core()), request.lengthInches(), optional(request.flexibility()),
                optional(request.maker()), request.description().strip(), request.priceGalleons(), request.stock().intValueExact(),
                images.store(files));
        return InventoryItem.from(products.save(product));
    }

    @Transactional
    public InventoryItem adjust(UUID id, int change) {
        if (change == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma quantidade diferente de zero");
        }
        WandProductEntity product = products.findByIdAndCustomerOwnerIdIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Varinha não encontrada no estoque"));
        if (!product.adjustStock(change)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O ajuste ultrapassa o limite de unidades do estoque");
        }
        return InventoryItem.from(product);
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public record NewWand(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 4000) String description,
            @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal priceGalleons,
            @NotNull @PositiveOrZero @Max(Integer.MAX_VALUE) @Digits(integer = 10, fraction = 0) BigDecimal stock,
            @Size(max = 80) String wood,
            @Size(max = 120) String core,
            @Positive @Digits(integer = 3, fraction = 2) BigDecimal lengthInches,
            @Size(max = 100) String flexibility,
            @Size(max = 120) String maker) {}

    public record InventoryItem(UUID id, String name, BigDecimal priceGalleons, int stock, boolean active) {
        static InventoryItem from(WandProductEntity product) {
            return new InventoryItem(product.getId(), product.getName(), product.getPriceGalleons(),
                    product.getStock(), product.isActive());
        }
    }

    public record InventoryPage(List<InventoryItem> items, int totalPages, long totalItems) {}
}
