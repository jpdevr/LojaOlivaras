package br.com.olivaras.api.cart.repository;

import br.com.olivaras.api.cart.entity.ShoppingCartEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, UUID> {
    Optional<ShoppingCartEntity> findByUser_Id(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ShoppingCartEntity c where c.user.id = :userId")
    Optional<ShoppingCartEntity> findByUserIdForUpdate(@Param("userId") UUID userId);

    void deleteByUser_Id(UUID userId);
}
