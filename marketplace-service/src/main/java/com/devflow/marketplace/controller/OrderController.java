package com.devflow.marketplace.controller;

import com.devflow.common.dto.ApiResponse;
import com.devflow.marketplace.dto.OrderDto;
import com.devflow.marketplace.dto.OrderRequest;
import com.devflow.marketplace.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        OrderDto order = orderService.placeOrder(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getMyOrders(
            @RequestHeader("X-User-Email") String userEmail) {

        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved",
                orderService.getMyOrders(userEmail)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String userEmail) {

        return ResponseEntity.ok(ApiResponse.success(
                "Order retrieved",
                orderService.getOrderById(id, userEmail)));
    }
}