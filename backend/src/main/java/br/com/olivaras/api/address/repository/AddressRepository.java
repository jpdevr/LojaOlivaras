package br.com.olivaras.api.address.repository;

import br.com.olivaras.api.address.entity.AddressEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
    List<AddressEntity> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);
    Optional<AddressEntity> findByIdAndUser_Id(UUID id, UUID userId);
    boolean existsByUser_IdAndDefaultAddressTrue(UUID userId);
    Optional<AddressEntity> findFirstByUser_IdOrderByCreatedAtAsc(UUID userId);
    void deleteAllByUser_Id(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update AddressEntity a set a.defaultAddress = false where a.user.id = :userId and a.defaultAddress = true")
    int clearDefaultForUser(@Param("userId") UUID userId);
}
