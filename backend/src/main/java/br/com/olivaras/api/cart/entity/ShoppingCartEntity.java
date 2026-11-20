package br.com.olivaras.api.cart.entity;

import br.com.olivaras.api.auth.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "shopping_cart")
public class ShoppingCartEntity {
    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
    private UserEntity user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ShoppingCartEntity() {}

    public ShoppingCartEntity(UserEntity user) { this.user = user; }

    @PrePersist
    void beforeInsert() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }

    @PreUpdate
    void beforeUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
}
