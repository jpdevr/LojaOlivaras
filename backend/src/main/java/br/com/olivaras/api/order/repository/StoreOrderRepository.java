package br.com.olivaras.api.order.repository;

import br.com.olivaras.api.order.entity.StoreOrderEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreOrderRepository extends JpaRepository<StoreOrderEntity, UUID> {
    Optional<StoreOrderEntity> findByIdAndCustomer_Id(UUID id, UUID customerId);
    Page<StoreOrderEntity> findAllByCustomer_IdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    @Modifying
    @Query("""
            update StoreOrderEntity o set o.recipient = 'Conta excluída', o.zipCode = '00000000',
                o.street = 'Dados removidos', o.streetNumber = 's/n', o.complement = null,
                o.district = 'Dados removidos', o.city = 'Dados removidos', o.state = '--',
                o.deliveryReference = null
            where o.customer.id = :customerId
            """)
    int anonymizeShippingData(@Param("customerId") UUID customerId);
}
