package com.devflow.marketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

// BigDecimal: a CLASS for exact decimal numbers (money).
import java.math.BigDecimal;
// Instant (CLASS) and UUID (CLASS)
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name="seller_id", nullable = false, updatable = false)
    private UUID sellerId;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 2000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductCategory category;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // [CONSTRUCTOR] The no-argument constructor JPA requires. "protected" discourages our own code from using it
    protected Product(){

    }

    // [CONSTRUCTOR] The one our code uses. [PARAMETER]s: everything a new product needs.
    public Product(UUID sellerId, String name, String description, ProductCategory category, BigDecimal price){
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = Instant.now();
    }

    // ---------- Domain methods: meaningful actions, instead of separate setters ----------

    // [METHOD] Replace all the editable details at once. No setSellerId(): ownership never changes.
    public void update(String name, String description, ProductCategory category, BigDecimal price){
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    // [METHOD] Soft delete: hide the product, keep the row.
    public void archive(){
        this.status = ProductStatus.ARCHIVED;
    }

    // [METHOD] Ownership check. Returns a boolean.
    //   [METHOD CALL] sellerId.equals(...): compares the VALUES of two UUID objects.
    //   (Never use == for objects: it checks whether they're the SAME object in memory, not equal values.)
    public boolean isOwnedBy(UUID userId){
        return sellerId.equals(userId);
    }

    // ---------- Getters ----------
    // [METHOD]s: read-only access to the FIELDs.
    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
