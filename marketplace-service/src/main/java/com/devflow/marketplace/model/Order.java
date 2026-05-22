package com.devflow.marketplace.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who placed this order (email from JWT)
    @Column(nullable = false)
    private String buyerEmail;

    // What they ordered
    @ManyToOne(fetch = FetchType.LAZY)
    // @ManyToOne = many orders can reference one product
    // FetchType.LAZY = don't load the product from DB
    // automatically — only when explicitly accessed
    // EAGER (default) would load product on every order query
    // LAZY is more efficient for lists of orders
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Price at time of purchase (product price may change later)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

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