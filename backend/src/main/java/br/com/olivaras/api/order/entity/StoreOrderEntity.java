package br.com.olivaras.api.order.entity;

import br.com.olivaras.api.address.entity.AddressEntity;
import br.com.olivaras.api.auth.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "store_order")
public class StoreOrderEntity {
    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private UserEntity customer;

    @Column(nullable = false, length = 30)
    private String status = "CONFIRMED";

    @Column(name = "payment_status", nullable = false, length = 40)
    private String paymentStatus = "PENDING";

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "payment_expires_at")
    private Instant paymentExpiresAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "payment_instructions", columnDefinition = "text")
    private String paymentInstructions;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 50)
    private String paymentReference;

    @Column(name = "total_galleons", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGalleons;

    @Column(nullable = false, length = 120)
    private String recipient;

    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;

    @Column(nullable = false, length = 160)
    private String street;

    @Column(name = "street_number", nullable = false, length = 30)
    private String streetNumber;

    @Column(length = 120)
    private String complement;

    @Column(nullable = false, length = 120)
    private String district;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "delivery_reference", length = 160)
    private String deliveryReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StoreOrderEntity() {}

    public StoreOrderEntity(UserEntity customer, AddressEntity address, BigDecimal totalGalleons,
                            String paymentReference, String paymentMethod, String paymentInstructions,
                            Instant paymentExpiresAt) {
        this.customer = customer;
        this.totalGalleons = totalGalleons;
        this.paymentReference = paymentReference;
        this.paymentMethod = paymentMethod;
        this.paymentInstructions = paymentInstructions;
        this.paymentExpiresAt = paymentExpiresAt;
        this.recipient = address.getRecipient();
        this.zipCode = address.getZipCode();
        this.street = address.getStreet();
        this.streetNumber = address.getNumber();
        this.complement = address.getComplement();
        this.district = address.getDistrict();
        this.city = address.getCity();
        this.state = address.getState();
        this.deliveryReference = address.getReference();
    }

    @PrePersist
    void beforeInsert() { createdAt = Instant.now(); }

    public void markPaymentApproved(Instant approvedAt) {
        this.paymentStatus = "SIMULATED_APPROVED";
        this.status = "PROCESSING";
        this.paidAt = approvedAt;
        this.estimatedDeliveryAt = approvedAt.plus(java.time.Duration.ofDays(1));
    }

    public void markDelivered(Instant deliveredAt) {
        this.status = "DELIVERED";
        this.deliveredAt = deliveredAt;
    }

    public UUID getId() { return id; }
    public UserEntity getCustomer() { return customer; }
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getPaymentExpiresAt() { return paymentExpiresAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getEstimatedDeliveryAt() { return estimatedDeliveryAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public String getPaymentInstructions() { return paymentInstructions; }
    public BigDecimal getTotalGalleons() { return totalGalleons; }
    public String getRecipient() { return recipient; }
    public String getZipCode() { return zipCode; }
    public String getStreet() { return street; }
    public String getStreetNumber() { return streetNumber; }
    public String getComplement() { return complement; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getDeliveryReference() { return deliveryReference; }
    public Instant getCreatedAt() { return createdAt; }
}
