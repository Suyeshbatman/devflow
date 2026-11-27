package com.devflow.marketplace.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Entity(name = "CustomerOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private UUID buyerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // @OneToMany: one Order has many OrderItems. This is the INVERSE side.
    //   mappedBy = "order": the relationship is defined by the "order" FIELD in OrderItem (the owning side).
    //   cascade = ALL: saving this Order also saves its items (and the same for deletes).
    //   orphanRemoval = true: an item removed from this list is DELETED from the database.
    //   (@OneToMany is LAZY by default, so items load only when they're used.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // [CONSTRUCTOR] Required by JPA.
    protected Order(){

    }
    // [CONSTRUCTOR] A new, empty order for this buyer. Items are added with addItem(...).
    public Order(UUID buyerId){
        this.buyerId = buyerId;
    }

    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
    }

    // [METHOD] The ONLY way to add an item. It keeps BOTH sides of the relationship in sync:
    //   the new item points at this order (this), AND this order's list contains the item.
    public void addItem(Product product, int quantity){
        OrderItem item = new OrderItem(this, product, quantity);
        items.add(item);
        recalcualteTotal();
    }

    // [METHOD] The total = the sum of all line totals, using a STREAM (a pipeline over a collection):
    //   .stream()                         → go through the items one by one
    //   .map(OrderItem::getLineTotal)     → turn each item into its line total (a METHOD REFERENCE)
    //   .reduce(BigDecimal.ZERO, BigDecimal::add) → start at 0 and add each value: 0 + a + b + c...
    //   It's like array_reduce in PHP, or Array.reduce in JS.
    public void recalcualteTotal(){
        this.totalAmount = items.stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isPlacedBy(UUID userId){
        return buyerId.equals(userId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    // [METHOD] Returns a READ-ONLY view of the list. Callers can't do getItems().add(...),
    //   which would bypass addItem() and leave the total and the relationship out of sync.
    //   [STATIC METHOD CALL] Collections.unmodifiableList(...)
    public List<OrderItem> getItems(){
        return Collections.unmodifiableList(items);
    }
}
