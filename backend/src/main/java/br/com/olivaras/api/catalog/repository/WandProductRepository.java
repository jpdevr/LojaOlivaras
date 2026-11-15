package br.com.olivaras.api.catalog.repository;

import br.com.olivaras.api.catalog.entity.WandProductEntity;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WandProductRepository extends JpaRepository<WandProductEntity, UUID> {
    Page<WandProductEntity> findAllByActiveTrue(Pageable pageable);

    Optional<WandProductEntity> findByIdAndActiveTrue(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from WandProductEntity p where p.id in :ids and p.active = true order by p.id")
    java.util.List<WandProductEntity> findActiveByIdsForUpdate(@Param("ids") java.util.Collection<UUID> ids);

    @Query("""
            select p from WandProductEntity p
            where p.active = true
              and (:term = '' or lower(p.name) like lower(concat('%', :term, '%'))
                   or lower(coalesce(p.ownerName, '')) like lower(concat('%', :term, '%'))
                   or lower(coalesce(p.description, '')) like lower(concat('%', :term, '%')))
              and (:wood = '' or lower(coalesce(p.wood, '')) like lower(concat('%', :wood, '%')))
              and (:core = '' or lower(coalesce(p.core, '')) like lower(concat('%', :core, '%')))
            """)
    Page<WandProductEntity> search(@Param("term") String term,
                                   @Param("wood") String wood,
                                   @Param("core") String core,
                                   Pageable pageable);
}
