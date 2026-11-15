package br.com.olivaras.api.order.service;

import br.com.olivaras.api.order.repository.StoreOrderRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderLifecycleService {
    private final StoreOrderRepository orders;

    public OrderLifecycleService(StoreOrderRepository orders) { this.orders = orders; }

    @Scheduled(fixedDelayString = "${app.orders.lifecycle-interval-ms:5000}")
    @Transactional
    public void advanceDueOrders() {
        Instant now = Instant.now();
        orders.approveDueSimulatedPayments(now);
        orders.deliverDueOrders(now);
    }
}
