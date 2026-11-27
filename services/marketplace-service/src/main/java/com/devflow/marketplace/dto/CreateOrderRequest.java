package com.devflow.marketplace.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "An order needs at least one item")
        @Size(max = 20, message = "An order can have at most 20 items")
        @Valid
        List<OrderLineRequest> items
) {
}
