package br.com.olivaras.api.cart.entity;

import br.com.olivaras.api.catalog.entity.WandProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "cart_item", uniqueConstraints = @UniqueConstraint(
        name = "cart_item_one_product_per_cart_uq", columnNames = {"cart_id", "product_id"}))
public class CartItemEntity {
    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCartEntity cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private WandProductEntity product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CartItemEntity() {}

    public CartItemEntity(ShoppingCartEntity cart, WandProductEntity product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    @PrePersist
    void beforeInsert() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }

    @PreUpdate
    void beforeUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public ShoppingCartEntity getCart() { return cart; }
    public WandProductEntity getProduct() { return product; }
    public int getQuantity() { return quantity; }
}
