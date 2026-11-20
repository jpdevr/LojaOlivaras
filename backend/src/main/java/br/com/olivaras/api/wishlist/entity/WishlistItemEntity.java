package br.com.olivaras.api.wishlist.entity;

import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.catalog.entity.WandProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "wishlist_item")
public class WishlistItemEntity {
    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private WandProductEntity product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WishlistItemEntity() {}

    @PrePersist
    void beforeInsert() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public WandProductEntity getProduct() { return product; }
    public Instant getCreatedAt() { return createdAt; }
}
