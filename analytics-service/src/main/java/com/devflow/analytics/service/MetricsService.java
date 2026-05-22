package com.devflow.analytics.service;

import com.devflow.analytics.dto.MetricsSummaryDto;
import com.devflow.analytics.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    // RedisTemplate = our Redis client
    private final RedisTemplate<String, Object> redisTemplate;

    // SimpMessagingTemplate = sends WebSocket messages
    // to connected browser clients
    private final SimpMessagingTemplate messagingTemplate;

    // Redis key constants — all metrics stored under these keys
    private static final String TOTAL_ORDERS = "analytics:total-orders";
    private static final String TOTAL_REVENUE = "analytics:total-revenue";
    private static final String ORDERS_TODAY = "analytics:orders-today";
    private static final String REVENUE_TODAY = "analytics:revenue-today";
    private static final String TOP_PRODUCTS = "analytics:top-products";
    private static final String ORDERS_BY_HOUR = "analytics:orders-by-hour";
    private static final String ACTIVE_USERS = "analytics:active-users-today";

    // Called by Kafka consumer when a new order event arrives
    // Updates all relevant Redis metrics atomically
    public void processOrderEvent(OrderEventDto event) {
        log.info("Processing order event: {} for order: {}",
                event.getEventType(), event.getOrderId());

        if ("ORDER_PLACED".equals(event.getEventType())) {
            updateOrderMetrics(event);
        }

        // After updating metrics, push latest summary
        // to all connected WebSocket clients
        // This is what makes the dashboard update in real-time
        pushMetricsUpdate();
    }

    private void updateOrderMetrics(OrderEventDto event) {
        // increment() = atomic increment in Redis
        // Thread-safe — even if multiple events arrive
        // simultaneously, each gets counted exactly once
        redisTemplate.opsForValue().increment(TOTAL_ORDERS);
        redisTemplate.opsForValue().increment(ORDERS_TODAY);

        // Update revenue — Redis stores as string, we parse
        updateRevenue(TOTAL_REVENUE, event.getPricePaid());
        updateRevenue(REVENUE_TODAY, event.getPricePaid());

        // Track top products using Redis Hash
        // opsForHash() = Redis hash operations (like a Map)
        // HINCRBY analytics:top-products "API Toolkit" 1
        redisTemplate.opsForHash().increment(
                TOP_PRODUCTS,
                event.getProductName(),
                1
        );

        // Track orders by hour for time-series chart
        // Key format: "2025-05-15T14" (date + hour)
        String hourKey = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH"));
        redisTemplate.opsForHash().increment(
                ORDERS_BY_HOUR, hourKey, 1);

        // Track unique buyers today using Redis Set
        // opsForSet() = Redis set (unique values only)
        // Adding same email twice counts as 1 unique user
        redisTemplate.opsForSet().add(
                ACTIVE_USERS, event.getBuyerEmail());

        log.info("Metrics updated for order: {}", event.getOrderId());
    }

    private void updateRevenue(String key, BigDecimal amount) {
        if (amount == null) return;
        Object current = redisTemplate.opsForValue().get(key);
        BigDecimal currentRevenue = current != null
                ? new BigDecimal(current.toString())
                : BigDecimal.ZERO;
        redisTemplate.opsForValue().set(key,
                currentRevenue.add(amount).toString());
    }

    // Reads all metrics from Redis and returns summary
    public MetricsSummaryDto getMetricsSummary() {
        Long totalOrders = getLongValue(TOTAL_ORDERS);
        Long ordersToday = getLongValue(ORDERS_TODAY);
        Long activeUsers = redisTemplate.opsForSet()
                .size(ACTIVE_USERS);

        String totalRevenueStr = getStringValue(TOTAL_REVENUE);
        String revenueTodayStr = getStringValue(REVENUE_TODAY);

        BigDecimal totalRevenue = totalRevenueStr != null
                ? new BigDecimal(totalRevenueStr) : BigDecimal.ZERO;
        BigDecimal revenueToday = revenueTodayStr != null
                ? new BigDecimal(revenueTodayStr) : BigDecimal.ZERO;

        // Get top products from Redis Hash
        Map<Object, Object> topProductsRaw =
                redisTemplate.opsForHash().entries(TOP_PRODUCTS);
        Map<String, Integer> topProducts = new HashMap<>();
        topProductsRaw.forEach((k, v) ->
                topProducts.put(k.toString(),
                        Integer.parseInt(v.toString())));

        // Get orders by hour from Redis Hash
        Map<Object, Object> byHourRaw =
                redisTemplate.opsForHash().entries(ORDERS_BY_HOUR);
        Map<String, Integer> ordersByHour = new HashMap<>();
        byHourRaw.forEach((k, v) ->
                ordersByHour.put(k.toString(),
                        Integer.parseInt(v.toString())));

        return MetricsSummaryDto.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .ordersToday(ordersToday)
                .revenueToday(revenueToday)
                .topProducts(topProducts)
                .ordersByHour(ordersByHour)
                .activeUsersToday(activeUsers)
                .build();
    }

    // Pushes current metrics to all WebSocket subscribers
    // React dashboard listens on /topic/analytics
    // Every time an order comes in, dashboard auto-updates
    private void pushMetricsUpdate() {
        MetricsSummaryDto summary = getMetricsSummary();
        messagingTemplate.convertAndSend(
                "/topic/analytics", summary);
        log.debug("Metrics pushed to WebSocket subscribers");
    }

    private Long getLongValue(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val.toString()) : 0L;
    }

    private String getStringValue(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        return val != null ? val.toString() : null;
    }
}