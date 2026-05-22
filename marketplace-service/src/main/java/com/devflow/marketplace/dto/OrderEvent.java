package com.devflow.marketplace.dto;

import com.devflow.marketplace.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// This is the message payload sent to Kafka
// When an order is placed, we publish this to
// the "order-events" topic
// Any service that needs to know about orders
// subscribes to that topic and receives this object
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    // Unique event identifier for deduplication
    private String eventId;

    // What type of event is this?
    // ORDER_PLACED, ORDER_CANCELLED, ORDER_CONFIRMED
    private String eventType;

    private Long orderId;
    private String buyerEmail;
    private Long productId;
    private String productName;
    private BigDecimal pricePaid;
    private OrderStatus status;

    // When did this event happen?
    private LocalDateTime occurredAt;
}