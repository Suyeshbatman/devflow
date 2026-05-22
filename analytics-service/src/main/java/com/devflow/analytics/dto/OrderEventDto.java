package com.devflow.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Mirror of marketplace-service's OrderEvent
// Analytics service deserializes Kafka messages
// into this object
// Must have same field names as the published event
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {
    private String eventId;
    private String eventType;
    private Long orderId;
    private String buyerEmail;
    private Long productId;
    private String productName;
    private BigDecimal pricePaid;
    private String status;
    private LocalDateTime occurredAt;
}