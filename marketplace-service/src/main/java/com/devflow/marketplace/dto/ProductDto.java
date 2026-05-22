package com.devflow.marketplace.dto;

import com.devflow.marketplace.model.ProductCategory;
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
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductCategory category;
    private String ownerEmail;
    private boolean active;
    private Integer purchaseCount;
    private LocalDateTime createdAt;
}