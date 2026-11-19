package br.com.olivaras.api.divination;

import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import br.com.olivaras.api.cart.repository.CartItemRepository;
import br.com.olivaras.api.cart.repository.ShoppingCartRepository;
import br.com.olivaras.api.catalog.dto.CatalogItemResponse;
import br.com.olivaras.api.catalog.repository.WandProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WandDivinationService {
    private final UserRepository users;
    private final WandProductRepository products;
    private final ShoppingCartRepository carts;
    private final CartItemRepository cartItems;

    public WandDivinationService(UserRepository users, WandProductRepository products,
                                 ShoppingCartRepository carts, CartItemRepository cartItems) {
        this.users = users;
        this.products = products;
        this.carts = carts;
        this.cartItems = cartItems;
    }

    @Transactional(readOnly = true)
    public Optional<CatalogItemResponse> current(UUID customerId) {
        return products.findByCustomerOwnerIdAndActiveTrue(customerId).map(CatalogItemResponse::from);
    }

    @Transactional
    public CatalogItemResponse reveal(UUID customerId, List<Integer> answers) {
        var customer = lockCustomer(customerId);
        var wand = products.findByCustomerOwnerIdAndActiveTrue(customerId).orElseGet(() ->
                products.save(WandQuiz.reveal(customerId, customer.getProfileName(), answers)));
        return CatalogItemResponse.from(wand);
    }

    @Transactional
    public void destroy(UUID customerId, UUID wandId) {
        lockCustomer(customerId);
        var wand = products.findByCustomerOwnerIdAndActiveTrue(customerId).orElse(null);
        if (wand == null) return;
        if (!wand.getId().equals(wandId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A varinha atual mudou. Reabra a Adivinhação.");
        }
        carts.findByUserIdForUpdate(customerId)
                .flatMap(cart -> cartItems.findByCart_IdAndProduct_Id(cart.getId(), wandId))
                .ifPresent(cartItems::delete);
        // Keep the product available as a historical reference for existing order lines.
        wand.retirePersonalWand();
    }

    private UserEntity lockCustomer(UUID customerId) {
        return users.findActiveByIdForUpdate(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }
}
