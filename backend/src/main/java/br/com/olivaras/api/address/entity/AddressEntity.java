package br.com.olivaras.api.address.entity;

import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.address.dto.AddressRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "customer_address")
public class AddressEntity {
    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Column(length = 40)
    private String label;

    @Column(nullable = false, length = 120)
    private String recipient;

    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;

    @Column(nullable = false, length = 160)
    private String street;

    @Column(name = "street_number", nullable = false, length = 30)
    private String number;

    @Column(length = 120)
    private String complement;

    @Column(nullable = false, length = 120)
    private String district;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "delivery_reference", length = 160)
    private String reference;

    @Column(name = "default_address", nullable = false)
    private boolean defaultAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AddressEntity() {}

    public AddressEntity(UserEntity user, AddressRequest request, boolean defaultAddress) {
        this.user = user;
        apply(request);
        this.defaultAddress = defaultAddress;
    }

    public void update(AddressRequest request, boolean defaultAddress) {
        apply(request);
        this.defaultAddress = defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    private void apply(AddressRequest request) {
        this.label = clean(request.label());
        this.recipient = request.recipient().trim();
        this.zipCode = request.zipCode().replaceAll("\\D", "");
        this.street = request.street().trim();
        this.number = request.number().trim();
        this.complement = clean(request.complement());
        this.district = request.district().trim();
        this.city = request.city().trim();
        this.state = request.state().trim().toUpperCase(java.util.Locale.ROOT);
        this.reference = clean(request.reference());
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    @PrePersist
    void beforeInsert() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate
    void beforeUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getLabel() { return label; }
    public String getRecipient() { return recipient; }
    public String getZipCode() { return zipCode; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getReference() { return reference; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
