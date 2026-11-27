package com.devflow.marketplace.dto;

import com.devflow.marketplace.entity.Product;
import com.devflow.marketplace.entity.ProductCategory;
import com.devflow.marketplace.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// [RECORD] What the API returns for one product.
public record ProductResponse (
        UUID id,
        UUID seller_id,
        String name,
        String description,
        ProductCategory category,
        BigDecimal price,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
){
    // [STATIC METHOD] entity → DTO, by calling the entity's getter METHODs.
    public static ProductResponse from (Product product){
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
