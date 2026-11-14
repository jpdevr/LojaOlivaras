package br.com.olivaras.api.order.entity;

import br.com.olivaras.api.catalog.entity.WandProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "store_order_item")
public class OrderItemEntity {
    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private StoreOrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private WandProductEntity product;

    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

    @Column(length = 80)
    private String wood;

    @Column(length = 120)
    private String core;

    @Column(name = "length_inches", precision = 5, scale = 2)
    private BigDecimal lengthInches;

    @Column(name = "unit_price_galleons", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceGalleons;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_total_galleons", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotalGalleons;

    protected OrderItemEntity() {}

    public OrderItemEntity(StoreOrderEntity order, WandProductEntity product, int quantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.wood = product.getWood();
        this.core = product.getCore();
        this.lengthInches = product.getLengthInches();
        this.unitPriceGalleons = product.getPriceGalleons();
        this.quantity = quantity;
        this.lineTotalGalleons = product.getPriceGalleons().multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() { return id; }
    public String getProductName() { return productName; }
    public String getWood() { return wood; }
    public String getCore() { return core; }
    public BigDecimal getLengthInches() { return lengthInches; }
    public BigDecimal getUnitPriceGalleons() { return unitPriceGalleons; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotalGalleons() { return lineTotalGalleons; }
}
