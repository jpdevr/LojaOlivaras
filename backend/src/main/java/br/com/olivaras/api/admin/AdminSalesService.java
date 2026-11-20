package br.com.olivaras.api.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSalesService {
    private static final ZoneId SALES_ZONE = ZoneId.of("America/Sao_Paulo");
    private final JdbcClient jdbc;

    public AdminSalesService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Dashboard dashboard(int days) {
        OffsetDateTime now = OffsetDateTime.now(SALES_ZONE);
        LocalDate firstDay = now.toLocalDate().minusDays(days - 1L);
        OffsetDateTime start = firstDay.atStartOfDay(SALES_ZONE).toOffsetDateTime();
        Map<LocalDate, DailySales> settled = jdbc.sql("""
                SELECT (o.paid_at AT TIME ZONE 'America/Sao_Paulo')::date AS day,
                       SUM(o.total_galleons) AS revenue, SUM(i.units) AS units
                FROM store_order o
                CROSS JOIN LATERAL (
                    SELECT COALESCE(SUM(quantity), 0) AS units
                    FROM store_order_item WHERE order_id = o.id
                ) i
                WHERE o.payment_status = 'SIMULATED_APPROVED' AND o.paid_at >= :start AND o.paid_at < :end
                GROUP BY day
                """).param("start", start).param("end", now)
                .query((row, index) -> new DailySales(row.getObject("day", LocalDate.class),
                        row.getBigDecimal("revenue"), row.getLong("units")))
                .stream().collect(Collectors.toMap(DailySales::date, Function.identity()));

        List<DailySales> dailySales = firstDay.datesUntil(now.toLocalDate().plusDays(1))
                .map(day -> settled.getOrDefault(day, new DailySales(day, BigDecimal.ZERO, 0)))
                .toList();
        long orderCount = jdbc.sql("""
                SELECT COUNT(*) FROM store_order WHERE created_at >= :start AND created_at < :end
                """).param("start", start).param("end", now).query(Long.class).single();
        List<RecentOrder> recentOrders = jdbc.sql("""
                SELECT id, created_at, payment_status, total_galleons
                FROM store_order
                WHERE created_at >= :start AND created_at < :end
                ORDER BY created_at DESC, id DESC
                LIMIT 10
                """).param("start", start).param("end", now)
                .query((row, index) -> new RecentOrder(row.getObject("id", UUID.class),
                        row.getObject("created_at", OffsetDateTime.class).toInstant(),
                        row.getString("payment_status"), row.getBigDecimal("total_galleons")))
                .list();

        BigDecimal revenue = dailySales.stream().map(DailySales::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        long units = dailySales.stream().mapToLong(DailySales::units).sum();
        return new Dashboard(revenue, orderCount, units, dailySales, recentOrders);
    }

    public record DailySales(LocalDate date, BigDecimal revenue, long units) {}

    public record RecentOrder(UUID id, Instant createdAt, String paymentStatus, BigDecimal total) {}

    public record Dashboard(BigDecimal approvedRevenue, long orderCount, long unitsSold,
                            List<DailySales> dailySales, List<RecentOrder> recentOrders) {}
}
