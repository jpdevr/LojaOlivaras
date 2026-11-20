package br.com.olivaras.api.auth.repository;

import br.com.olivaras.api.auth.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByIdAndActiveTrue(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.id = :id and u.active = true")
    Optional<UserEntity> findActiveByIdForUpdate(@Param("id") UUID id);
    boolean existsByEmailIgnoreCase(String email);
}
