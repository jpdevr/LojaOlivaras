package br.com.olivaras.api.cart.repository;

import br.com.olivaras.api.cart.entity.CartItemEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {
    @EntityGraph(attributePaths = "product")
    List<CartItemEntity> findAllByCart_IdOrderByProduct_NameAsc(UUID cartId);

    @EntityGraph(attributePaths = "product")
    Optional<CartItemEntity> findByCart_IdAndProduct_Id(UUID cartId, UUID productId);

    void deleteAllByCart_Id(UUID cartId);
}
