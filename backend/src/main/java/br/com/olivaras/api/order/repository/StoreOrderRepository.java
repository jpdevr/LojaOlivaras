package br.com.olivaras.api.order.repository;

import br.com.olivaras.api.order.entity.StoreOrderEntity;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreOrderRepository extends JpaRepository<StoreOrderEntity, UUID> {
    Optional<StoreOrderEntity> findByIdAndCustomer_Id(UUID id, UUID customerId);
    Page<StoreOrderEntity> findAllByCustomer_IdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update store_order
            set payment_status = 'SIMULATED_APPROVED',
                status = 'PROCESSING',
                paid_at = :now,
                estimated_delivery_at = CAST(:now AS timestamptz) + interval '1 day'
            where payment_status = 'PENDING'
              and payment_expires_at <= :now
            """, nativeQuery = true)
    int approveDueSimulatedPayments(@Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update store_order
            set status = 'DELIVERED', delivered_at = :now
            where status = 'PROCESSING'
              and estimated_delivery_at <= :now
            """, nativeQuery = true)
    int deliverDueOrders(@Param("now") Instant now);

    @Modifying
    @Query("""
            update StoreOrderEntity o set o.recipient = 'Conta excluída', o.zipCode = '00000000',
                o.street = 'Dados removidos', o.streetNumber = 's/n', o.complement = null,
                o.district = 'Dados removidos', o.city = 'Dados removidos', o.state = '--',
                o.deliveryReference = null, o.customerCpf = null
            where o.customer.id = :customerId
            """)
    int anonymizeShippingData(@Param("customerId") UUID customerId);
}
