package br.com.olivaras.api.auth.repository;

import br.com.olivaras.api.auth.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByGoogleSubject(String googleSubject);
    boolean existsByEmailIgnoreCase(String email);
}
