package com.devflow.marketplace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;
}