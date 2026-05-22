package com.devflow.marketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    // TEXT = unlimited length string in PostgreSQL
    // default VARCHAR has a length limit
    private String description;

    // BigDecimal for money — NEVER use float or double
    // for currency because of floating point precision errors
    // e.g. 0.1 + 0.2 = 0.30000000000000004 in float
    // BigDecimal is exact
    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    // Who created/owns this product
    // We store the email from the JWT token
    // (passed by gateway as X-User-Email header)
    @Column(nullable = false)
    private String ownerEmail;

    // Is this product visible in the marketplace?
    @Builder.Default
    private boolean active = true;

    // How many times has this been purchased?
    @Builder.Default
    private Integer purchaseCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}