package br.com.olivaras.api.cart.service;

import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import br.com.olivaras.api.cart.dto.AddCartItemRequest;
import br.com.olivaras.api.cart.dto.CartLineResponse;
import br.com.olivaras.api.cart.dto.CartResponse;
import br.com.olivaras.api.cart.dto.RemoveCartItemRequest;
import br.com.olivaras.api.cart.dto.UpdateCartQuantityRequest;
import br.com.olivaras.api.cart.entity.CartItemEntity;
import br.com.olivaras.api.cart.entity.ShoppingCartEntity;
import br.com.olivaras.api.cart.repository.CartItemRepository;
import br.com.olivaras.api.cart.repository.ShoppingCartRepository;
import br.com.olivaras.api.catalog.entity.WandProductEntity;
import br.com.olivaras.api.catalog.repository.WandProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
    private final UserRepository users;
    private final ShoppingCartRepository carts;
    private final CartItemRepository items;
    private final WandProductRepository products;

    public CartService(UserRepository users, ShoppingCartRepository carts,
                       CartItemRepository items, WandProductRepository products) {
        this.users = users;
        this.carts = carts;
        this.items = items;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public CartResponse get(UUID userId) {
        List<CartItemEntity> lines = carts.findByUser_Id(userId)
                .map(cart -> items.findAllByCart_IdOrderByProduct_NameAsc(cart.getId()))
                .orElseGet(List::of);
        return response(lines);
    }

    @Transactional
    public CartResponse add(UUID userId, AddCartItemRequest request) {
        ShoppingCartEntity cart = lockedOrCreateCart(userId);
        WandProductEntity product = activeProduct(request.itemId());
        CartItemEntity line = items.findByCart_IdAndProduct_Id(cart.getId(), product.getId()).orElse(null);
        int newQuantity = request.quantity() + (line == null ? 0 : line.getQuantity());
        ensureStock(product, newQuantity);
        if (line == null) line = new CartItemEntity(cart, product, newQuantity);
        else line.setQuantity(newQuantity);
        items.saveAndFlush(line);
        return get(userId);
    }

    @Transactional
    public CartResponse updateQuantity(UUID userId, UUID itemId, UpdateCartQuantityRequest request) {
        ShoppingCartEntity cart = lockedExistingCart(userId);
        CartItemEntity line = ownedLine(cart.getId(), itemId);
        ensureStock(activeProduct(itemId), request.quantity());
        line.setQuantity(request.quantity());
        items.saveAndFlush(line);
        return get(userId);
    }

    @Transactional
    public CartResponse remove(UUID userId, RemoveCartItemRequest request) {
        ShoppingCartEntity cart = lockedExistingCart(userId);
        items.delete(ownedLine(cart.getId(), request.itemId()));
        items.flush();
        return get(userId);
    }

    @Transactional
    public CartResponse clear(UUID userId) {
        ShoppingCartEntity cart = carts.findByUserIdForUpdate(userId).orElse(null);
        if (cart != null) items.deleteAllByCart_Id(cart.getId());
        return get(userId);
    }

    private ShoppingCartEntity lockedOrCreateCart(UUID userId) {
        UserEntity user = users.findActiveByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
        return carts.findByUserIdForUpdate(userId).orElseGet(() -> carts.saveAndFlush(new ShoppingCartEntity(user)));
    }

    private ShoppingCartEntity lockedExistingCart(UUID userId) {
        users.findActiveByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
        return carts.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho vazio"));
    }

    private CartItemEntity ownedLine(UUID cartId, UUID itemId) {
        return items.findByCart_IdAndProduct_Id(cartId, itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não está no carrinho"));
    }

    private WandProductEntity activeProduct(UUID id) {
        return products.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));
    }

    private void ensureStock(WandProductEntity product, int quantity) {
        if (quantity > product.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quantidade solicitada indisponível em estoque");
        }
    }

    private CartResponse response(List<CartItemEntity> lines) {
        List<CartLineResponse> responses = lines.stream().map(CartLineResponse::from).toList();
        BigDecimal total = responses.stream().map(CartLineResponse::lineTotalGalleons)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = responses.stream().mapToInt(CartLineResponse::quantity).sum();
        return new CartResponse(responses, total, count);
    }
}
