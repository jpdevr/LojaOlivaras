package br.com.olivaras.api.order.service;

import br.com.olivaras.api.address.dto.AddressResponse;
import br.com.olivaras.api.address.entity.AddressEntity;
import br.com.olivaras.api.address.repository.AddressRepository;
import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import br.com.olivaras.api.cart.entity.CartItemEntity;
import br.com.olivaras.api.cart.entity.ShoppingCartEntity;
import br.com.olivaras.api.cart.repository.CartItemRepository;
import br.com.olivaras.api.cart.repository.ShoppingCartRepository;
import br.com.olivaras.api.catalog.entity.WandProductEntity;
import br.com.olivaras.api.catalog.repository.WandProductRepository;
import br.com.olivaras.api.order.dto.CheckoutRequest;
import br.com.olivaras.api.order.dto.OrderItemResponse;
import br.com.olivaras.api.order.dto.OrderResponse;
import br.com.olivaras.api.order.dto.PaymentStatusResponse;
import br.com.olivaras.api.order.entity.OrderItemEntity;
import br.com.olivaras.api.order.entity.PaymentMethod;
import br.com.olivaras.api.order.entity.StoreOrderEntity;
import br.com.olivaras.api.order.repository.OrderItemRepository;
import br.com.olivaras.api.order.repository.StoreOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CheckoutService {
    private final UserRepository users;
    private final AddressRepository addresses;
    private final ShoppingCartRepository carts;
    private final CartItemRepository cartItems;
    private final WandProductRepository products;
    private final StoreOrderRepository orders;
    private final OrderItemRepository orderItems;
    private final OrderLifecycleService lifecycle;
    private final PaymentQrCodeService qrCodes;
    private final BoletoPdfService boletoPdf;

    public CheckoutService(UserRepository users, AddressRepository addresses, ShoppingCartRepository carts,
                           CartItemRepository cartItems, WandProductRepository products,
                           StoreOrderRepository orders, OrderItemRepository orderItems,
                           OrderLifecycleService lifecycle, PaymentQrCodeService qrCodes,
                           BoletoPdfService boletoPdf) {
        this.users = users;
        this.addresses = addresses;
        this.carts = carts;
        this.cartItems = cartItems;
        this.products = products;
        this.orders = orders;
        this.orderItems = orderItems;
        this.lifecycle = lifecycle;
        this.qrCodes = qrCodes;
        this.boletoPdf = boletoPdf;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> checkoutAddresses(UUID userId) {
        requireActiveUser(userId);
        return addresses.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(AddressResponse::from).toList();
    }

    @Transactional
    public OrderResponse confirm(UUID userId, CheckoutRequest request) {
        UserEntity user = users.findActiveByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
        ShoppingCartEntity cart = carts.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Carrinho vazio"));
        AddressEntity address = addresses.findByIdAndUser_Id(request.addressId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado"));
        List<CartItemEntity> lines = cartItems.findAllByCart_IdOrderByProduct_NameAsc(cart.getId());
        if (request.itemIds() != null) {
            var selected = new java.util.HashSet<>(request.itemIds());
            lines = lines.stream().filter(line -> selected.contains(line.getProduct().getId())).toList();
            if (lines.size() != selected.size()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A seleção mudou. Revise seu carrinho.");
            }
        }
        if (lines.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Selecione itens no carrinho");

        List<UUID> productIds = lines.stream().map(line -> line.getProduct().getId()).distinct().sorted().toList();
        List<WandProductEntity> lockedProducts = products.findPurchasableForUpdate(productIds, userId);
        Map<UUID, WandProductEntity> productById = lockedProducts.stream()
                .collect(Collectors.toMap(WandProductEntity::getId, Function.identity()));
        if (productById.size() != productIds.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Um ou mais produtos não estão mais disponíveis");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemEntity line : lines) {
            WandProductEntity product = productById.get(line.getProduct().getId());
            if (product.getStock() < line.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Estoque insuficiente para o item: " + product.getName());
            }
            total = total.add(product.getPriceGalleons().multiply(BigDecimal.valueOf(line.getQuantity())));
        }

        Instant now = Instant.now();
        Instant paymentExpiresAt = now.plus(request.paymentMethod().simulationDelay());
        String paymentReference = "SIM-" + UUID.randomUUID();
        String paymentInstructions = paymentInstructions(request.paymentMethod(), paymentReference, total,
                paymentExpiresAt);
        StoreOrderEntity order = orders.saveAndFlush(new StoreOrderEntity(user, address, total,
                paymentReference, request.paymentMethod().name(), paymentInstructions, paymentExpiresAt));
        for (CartItemEntity line : lines) {
            WandProductEntity product = productById.get(line.getProduct().getId());
            product.decreaseStock(line.getQuantity());
            orderItems.save(new OrderItemEntity(order, product, line.getQuantity()));
        }
        products.flush();
        orderItems.flush();
        cartItems.deleteAll(lines);
        cartItems.flush();

        return response(order);
    }

    @Transactional
    public OrderResponse order(UUID userId, UUID orderId) {
        requireActiveUser(userId);
        lifecycle.advanceDueOrders();
        StoreOrderEntity order = ownedOrder(userId, orderId);
        return response(order);
    }

    @Transactional
    public PaymentStatusResponse paymentStatus(UUID userId, UUID orderId) {
        requireActiveUser(userId);
        lifecycle.advanceDueOrders();
        StoreOrderEntity order = ownedOrder(userId, orderId);
        String qrCode = "PIX".equals(order.getPaymentMethod())
                && "PENDING".equals(order.getPaymentStatus())
                ? qrCodes.toDataUri(order.getPaymentInstructions())
                : null;
        return PaymentStatusResponse.from(order, qrCode, Instant.now());
    }

    @Transactional
    public Page<OrderResponse> orderHistory(UUID userId, int page, int size) {
        requireActiveUser(userId);
        lifecycle.advanceDueOrders();
        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use page >= 0 e size entre 1 e 50");
        }
        return orders.findAllByCustomer_IdOrderByCreatedAtDesc(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::response);
    }

    @Transactional
    public byte[] boletoPdf(UUID userId, UUID orderId) {
        requireActiveUser(userId);
        lifecycle.advanceDueOrders();
        StoreOrderEntity order = ownedOrder(userId, orderId);
        if (!"BOLETO".equals(order.getPaymentMethod())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este pedido nao utiliza boleto simulado");
        }
        List<OrderItemResponse> items = orderItems.findAllByOrder_IdOrderByProductNameAsc(orderId).stream()
                .map(OrderItemResponse::from).toList();
        return boletoPdf.create(order, items);
    }

    private String paymentInstructions(PaymentMethod method, String reference, BigDecimal total,
                                       Instant paymentExpiresAt) {
        return switch (method) {
            case PIX -> "OLIVARAS-PIX-SIMULADO|" + reference + "|" + total.toPlainString()
                    + "|" + paymentExpiresAt;
            case BOLETO -> "OLIVARAS-BOLETO-SIMULADO|" + reference;
            case CREDIT_CARD -> "OLIVARAS-CARTAO-SIMULADO|" + reference;
        };
    }

    private OrderResponse response(StoreOrderEntity order) {
        List<OrderItemResponse> lines = orderItems.findAllByOrder_IdOrderByProductNameAsc(order.getId()).stream()
                .map(OrderItemResponse::from).toList();
        return OrderResponse.from(order, lines);
    }

    private StoreOrderEntity ownedOrder(UUID userId, UUID orderId) {
        return orders.findByIdAndCustomer_Id(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }

    private UserEntity requireActiveUser(UUID userId) {
        return users.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }
}
