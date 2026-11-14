package br.com.olivaras.api.wishlist.repository;

import br.com.olivaras.api.wishlist.entity.WishlistItemEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<WishlistItemEntity, UUID> {
    @Modifying
    @Query(value = """
            insert into wishlist_item (id, user_id, product_id, created_at)
            values (gen_random_uuid(), :userId, :productId, now())
            on conflict (user_id, product_id) do nothing
            """, nativeQuery = true)
    int addIfAbsent(@Param("userId") UUID userId, @Param("productId") UUID productId);

    @EntityGraph(attributePaths = "product")
    List<WishlistItemEntity> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "product")
    Optional<WishlistItemEntity> findByUser_IdAndProduct_Id(UUID userId, UUID productId);

    @Modifying
    @Query("delete from WishlistItemEntity w where w.user.id = :userId and w.product.id = :productId")
    int removeProduct(@Param("userId") UUID userId, @Param("productId") UUID productId);

    @Modifying
    @Query("delete from WishlistItemEntity w where w.user.id = :userId")
    int clearForUser(@Param("userId") UUID userId);
}
