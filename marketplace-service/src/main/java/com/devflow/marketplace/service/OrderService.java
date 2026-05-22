package com.devflow.marketplace.service;

import com.devflow.common.enums.ErrorCode;
import com.devflow.common.exception.BaseException;
import com.devflow.marketplace.dto.OrderDto;
import com.devflow.marketplace.dto.OrderEvent;
import com.devflow.marketplace.dto.OrderRequest;
import com.devflow.marketplace.kafka.OrderEventProducer;
import com.devflow.marketplace.model.Order;
import com.devflow.marketplace.model.OrderStatus;
import com.devflow.marketplace.model.Product;
import com.devflow.marketplace.repository.OrderRepository;
import com.devflow.marketplace.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEventProducer orderEventProducer;
    private final ProductService productService;

    @Transactional
    public OrderDto placeOrder(OrderRequest request, String buyerEmail) {
        log.info("Placing order for product: {} by: {}",
                request.getProductId(), buyerEmail);

        // Step 1: Find the product
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new BaseException(ErrorCode.PRODUCT_NOT_FOUND));

        // Step 2: Check product is still available
        if (!product.isActive()) {
            throw new BaseException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Step 3: Create the order
        Order order = Order.builder()
                .buyerEmail(buyerEmail)
                .product(product)
                // Snapshot the price at time of purchase
                // If seller changes price later, order history
                // still shows what buyer actually paid
                .pricePaid(product.getPrice())
                .status(OrderStatus.CONFIRMED)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Step 4: Increment product purchase count
        product.setPurchaseCount(product.getPurchaseCount() + 1);
        productRepository.save(product);

        // Step 5: Publish event to Kafka
        // This is fire-and-forget — we don't wait for consumers
        // analytics-service and notification-service will
        // consume this event asynchronously
        OrderEvent event = OrderEvent.builder()
                // UUID = Universally Unique Identifier
                // Ensures each event has a unique ID
                // Consumers use this for deduplication
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_PLACED")
                .orderId(savedOrder.getId())
                .buyerEmail(buyerEmail)
                .productId(product.getId())
                .productName(product.getName())
                .pricePaid(product.getPrice())
                .status(OrderStatus.CONFIRMED)
                .occurredAt(LocalDateTime.now())
                .build();

        orderEventProducer.publishOrderEvent(event);

        log.info("Order placed successfully: {}", savedOrder.getId());
        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getMyOrders(String buyerEmail) {
        return orderRepository.findByBuyerEmail(buyerEmail)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id, String buyerEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.ORDER_NOT_FOUND));

        // Users can only see their own orders
        if (!order.getBuyerEmail().equals(buyerEmail)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        return mapToDto(order);
    }

    private OrderDto mapToDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .buyerEmail(order.getBuyerEmail())
                .product(productService.mapToDto(order.getProduct()))
                .pricePaid(order.getPricePaid())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}