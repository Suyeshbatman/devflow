package com.devflow.marketplace.controller;

import com.devflow.marketplace.dto.CreateOrderRequest;
import com.devflow.marketplace.dto.OrderResponse;
import com.devflow.marketplace.dto.PageResponse;
import com.devflow.marketplace.security.CurrentUser;
import com.devflow.marketplace.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;


// Every route needs a valid token (anyRequest().authenticated() in SecurityConfig).
// Any role may buy, including VIEWER.
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        OrderResponse created = orderService.placeOrder(request, CurrentUser.from(jwt));
        return ResponseEntity.created(URI.create("/api/orders/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<OrderResponse> listMine(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return orderService.listMine(CurrentUser.from(jwt), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt){
        return orderService.getById(id, CurrentUser.from(jwt));
    }
}
