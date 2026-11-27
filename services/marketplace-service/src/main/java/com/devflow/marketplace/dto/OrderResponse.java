package com.devflow.marketplace.dto;

import com.devflow.marketplace.entity.Order;
import com.devflow.marketplace.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// [RECORD] A full order with its items.
public record OrderResponse(
    UUID id,
    UUID buyerId,
    OrderStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    List<OrderItemResponse> items
) {
    // [STATIC METHOD] entity → DTO. ⚠Calling order.getItems() triggers the LAZY load, so this
    //   MUST run inside a transaction (in the service), or you'd get a LazyInitializationException.
    public static OrderResponse from(Order order){
        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                // [METHOD CALL] stream → map each OrderItem to an OrderItemResponse → .toList()
                //   collects the results into a new, unmodifiable List.
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
