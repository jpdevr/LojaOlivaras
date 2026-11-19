package br.com.olivaras.api.wishlist.service;

import br.com.olivaras.api.auth.repository.UserRepository;
import br.com.olivaras.api.catalog.repository.WandProductRepository;
import br.com.olivaras.api.wishlist.dto.WishlistItemRequest;
import br.com.olivaras.api.wishlist.dto.WishlistItemResponse;
import br.com.olivaras.api.wishlist.dto.WishlistResponse;
import br.com.olivaras.api.wishlist.repository.WishlistRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WishlistService {
    private final UserRepository users;
    private final WandProductRepository products;
    private final WishlistRepository wishlist;

    public WishlistService(UserRepository users, WandProductRepository products, WishlistRepository wishlist) {
        this.users = users;
        this.products = products;
        this.wishlist = wishlist;
    }

    @Transactional(readOnly = true)
    public WishlistResponse list(UUID userId) {
        requireActiveUser(userId);
        var items = wishlist.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(WishlistItemResponse::from).toList();
        return new WishlistResponse(items, items.size());
    }

    @Transactional
    public WishlistItemResponse add(UUID userId, WishlistItemRequest request) {
        requireActiveUser(userId);
        products.findByIdAndActiveTrueAndCustomerOwnerIdIsNull(request.itemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));
        wishlist.addIfAbsent(userId, request.itemId());
        return wishlist.findByUser_IdAndProduct_Id(userId, request.itemId())
                .map(WishlistItemResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Não foi possível salvar o item na wishlist"));
    }

    @Transactional
    public WishlistResponse remove(UUID userId, WishlistItemRequest request) {
        requireActiveUser(userId);
        wishlist.removeProduct(userId, request.itemId());
        return list(userId);
    }

    @Transactional
    public WishlistResponse clear(UUID userId) {
        requireActiveUser(userId);
        wishlist.clearForUser(userId);
        return new WishlistResponse(java.util.List.of(), 0);
    }

    private void requireActiveUser(UUID userId) {
        users.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }
}
