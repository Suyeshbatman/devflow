package com.devflow.marketplace.dto;

import com.devflow.marketplace.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String buyerEmail;
    private ProductDto product;
    private BigDecimal pricePaid;
    private OrderStatus status;
    private LocalDateTime createdAt;
}