package br.com.olivaras.api.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "wand_product")
public class WandProductEntity {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "owner_name", length = 120)
    private String ownerName;

    @Column(name = "customer_owner_id", updatable = false)
    private UUID customerOwnerId;

    @Column(length = 80)
    private String wood;

    @Column(length = 120)
    private String core;

    @Column(name = "length_inches", precision = 5, scale = 2)
    private BigDecimal lengthInches;

    @Column(length = 100)
    private String flexibility;

    @Column(length = 120)
    private String maker;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "canon_tier", nullable = false, length = 40)
    private String canonTier;

    @Column(name = "source_reference", length = 500)
    private String sourceReference;

    @Column(name = "price_galleons", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceGalleons;

    @Column(nullable = false)
    private int stock;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "additional_image_urls", nullable = false, columnDefinition = "text[]")
    private String[] additionalImageUrls = new String[0];

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WandProductEntity() {}

    public static WandProductEntity forSale(String name, String wood, String core, BigDecimal length,
                                            String flexibility, String maker, String description,
                                            BigDecimal price, int stock, List<String> imageUrls) {
        WandProductEntity wand = new WandProductEntity();
        wand.slug = "loja-" + UUID.randomUUID();
        wand.name = name;
        wand.wood = wood;
        wand.core = core;
        wand.lengthInches = length;
        wand.flexibility = flexibility;
        wand.maker = maker;
        wand.description = description;
        wand.canonTier = "STORE_ORIGINAL";
        wand.priceGalleons = price;
        wand.stock = stock;
        wand.imageUrl = imageUrls.getFirst();
        wand.additionalImageUrls = imageUrls.subList(1, imageUrls.size()).toArray(String[]::new);
        wand.active = true;
        return wand;
    }

    public static WandProductEntity personalWand(UUID customerId, String ownerName, String wood, String core,
                                                BigDecimal length, String flexibility, String description,
                                                String imageUrl) {
        WandProductEntity wand = new WandProductEntity();
        wand.slug = "personal-" + UUID.randomUUID();
        wand.name = "Varinha pessoal";
        wand.ownerName = ownerName;
        wand.customerOwnerId = customerId;
        wand.wood = wood;
        wand.core = core;
        wand.lengthInches = length;
        wand.flexibility = flexibility;
        wand.maker = "Ateliê Magia Olivaras";
        wand.description = description;
        wand.canonTier = "PERSONAL";
        wand.priceGalleons = BigDecimal.valueOf(7);
        wand.imageUrl = imageUrl;
        wand.stock = 1;
        wand.active = true;
        return wand;
    }

    public void retirePersonalWand() {
        ownerName = null;
        stock = 0;
        active = false;
    }

    public void decreaseStock(int quantity) {
        if (quantity < 1 || stock < quantity) throw new IllegalArgumentException("Estoque insuficiente");
        stock -= quantity;
    }

    public boolean adjustStock(int change) {
        long adjusted = (long) stock + change;
        if (adjusted < 0 || adjusted > Integer.MAX_VALUE) return false;
        stock = (int) adjusted;
        return true;
    }

    @PrePersist
    void beforeInsert() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }

    @PreUpdate
    void beforeUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getOwnerName() { return ownerName; }
    public String getWood() { return wood; }
    public String getCore() { return core; }
    public BigDecimal getLengthInches() { return lengthInches; }
    public String getFlexibility() { return flexibility; }
    public String getMaker() { return maker; }
    public String getDescription() { return description; }
    public String getCanonTier() { return canonTier; }
    public String getSourceReference() { return sourceReference; }
    public BigDecimal getPriceGalleons() { return priceGalleons; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImageUrls() {
        if (imageUrl == null) return List.of();
        List<String> gallery = new ArrayList<>(1 + additionalImageUrls.length);
        gallery.add(imageUrl);
        Collections.addAll(gallery, additionalImageUrls);
        return gallery;
    }
    public boolean isActive() { return active; }
}
