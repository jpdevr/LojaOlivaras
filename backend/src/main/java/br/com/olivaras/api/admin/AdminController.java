package br.com.olivaras.api.admin;

import br.com.olivaras.api.admin.AdminInventoryService.InventoryItem;
import br.com.olivaras.api.admin.AdminInventoryService.InventoryPage;
import br.com.olivaras.api.admin.AdminInventoryService.NewWand;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/lojaolivaras/api/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administração", description = "Vendas e estoque da loja")
public class AdminController {
    private final AdminSalesService sales;
    private final AdminInventoryService inventory;

    public AdminController(AdminSalesService sales, AdminInventoryService inventory) {
        this.sales = sales;
        this.inventory = inventory;
    }

    @GetMapping("/sales")
    public AdminSalesService.Dashboard sales(@RequestParam(defaultValue = "30") @Min(1) @Max(90) int days) {
        return sales.dashboard(days);
    }

    @GetMapping("/inventory")
    public InventoryPage inventory(@RequestParam(defaultValue = "") @Size(max = 160) String search,
                                   @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page) {
        return inventory.list(search, page);
    }

    @PostMapping(value = "/inventory", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItem create(@Valid @RequestPart("product") NewWand request,
                                @RequestPart("images") List<MultipartFile> images) {
        return inventory.create(request, images);
    }

    @PatchMapping("/inventory/{id}/stock")
    public InventoryItem adjust(@PathVariable UUID id, @Valid @RequestBody StockAdjustment request) {
        return inventory.adjust(id, request.change().intValueExact());
    }

    public record StockAdjustment(
            @NotNull @Min(Integer.MIN_VALUE) @Max(Integer.MAX_VALUE)
            @Digits(integer = 10, fraction = 0) BigDecimal change) {}
}
