package br.com.olivaras.api.order.service;

import br.com.olivaras.api.cart.repository.ShoppingCartRepository;
import br.com.olivaras.api.order.repository.StoreOrderRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerCommerceCleanupService {
    private final ShoppingCartRepository carts;
    private final StoreOrderRepository orders;

    public CustomerCommerceCleanupService(ShoppingCartRepository carts, StoreOrderRepository orders) {
        this.carts = carts;
        this.orders = orders;
    }

    @Transactional
    public void anonymize(UUID userId) {
        carts.deleteByUser_Id(userId);
        orders.anonymizeShippingData(userId);
    }
}
