package br.com.olivaras.api.catalog.repository;

import br.com.olivaras.api.catalog.entity.WandProductEntity;
import java.util.List;
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

    @Query(value = """
            select candidate.*
            from wand_product selected
            join wand_product candidate on candidate.active and candidate.id <> selected.id
            cross join lateral (
                select
                    case when lower(candidate.core) = lower(selected.core) then 6 else 0 end
                    + case when lower(candidate.wood) = lower(selected.wood) then 5 else 0 end
                    + case when lower(split_part(candidate.owner_name, ' (', 1))
                              = lower(split_part(selected.owner_name, ' (', 1)) then 4 else 0 end
                    + case when abs(candidate.length_inches - selected.length_inches) <= 1 then 3 else 0 end
                    + case when lower(candidate.maker) = lower(selected.maker) then 2 else 0 end
                    + case when lower(candidate.flexibility) = lower(selected.flexibility) then 1 else 0 end
                    + case when candidate.canon_tier = selected.canon_tier then 1 else 0 end
                    as score
            ) relevance
            where selected.id = :id and selected.active and relevance.score > 0
            order by relevance.score desc, candidate.name, candidate.id
            limit 3
            """, nativeQuery = true)
    List<WandProductEntity> findRelated(@Param("id") UUID id);

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
