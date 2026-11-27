package com.devflow.marketplace.dto;

import com.devflow.marketplace.entity.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// [RECORD] The JSON body for POST and PUT /api/products.
//   There's no sellerId field: the seller ALWAYS comes from the token, never from the client.
public record ProductRequest (
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,
        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,
        @NotNull(message = "Category is required")
        ProductCategory category,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimals")
        BigDecimal price
){
    public ProductRequest{
        if(name != null){
            name = name.trim();
        }
        if(description != null){
            description = description.trim();
        }
    }
}
