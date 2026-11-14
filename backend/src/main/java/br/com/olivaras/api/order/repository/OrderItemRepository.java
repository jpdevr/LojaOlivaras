package br.com.olivaras.api.order.repository;

import br.com.olivaras.api.order.entity.OrderItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
    List<OrderItemEntity> findAllByOrder_IdOrderByProductNameAsc(UUID orderId);
}
