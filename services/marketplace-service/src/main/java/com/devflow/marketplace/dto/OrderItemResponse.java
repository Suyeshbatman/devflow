package com.devflow.marketplace.dto;

import com.devflow.marketplace.entity.OrderItem;
import java.math.BigDecimal;
import java.util.UUID;

// [RECORD] One line of an order, as returned by the API.
public record OrderItemResponse(
        UUID productId,
        String productName,
        UUID sellerId,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
    // [STATIC METHOD] entity → DTO
    public static OrderItemResponse from(OrderItem item){
        return new OrderItemResponse(
                item.getProductId(),
                item.getProdctName(),
                item.getSellerId(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
