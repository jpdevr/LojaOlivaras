package br.com.olivaras.api.order.service;

import br.com.olivaras.api.cart.repository.ShoppingCartRepository;
import br.com.olivaras.api.order.repository.StoreOrderRepository;
import br.com.olivaras.api.wishlist.repository.WishlistRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerCommerceCleanupService {
    private final ShoppingCartRepository carts;
    private final StoreOrderRepository orders;
    private final WishlistRepository wishlists;

    public CustomerCommerceCleanupService(ShoppingCartRepository carts, StoreOrderRepository orders,
                                          WishlistRepository wishlists) {
        this.carts = carts;
        this.orders = orders;
        this.wishlists = wishlists;
    }

    @Transactional
    public void anonymize(UUID userId) {
        carts.deleteByUser_Id(userId);
        wishlists.clearForUser(userId);
        orders.anonymizeShippingData(userId);
    }
}
