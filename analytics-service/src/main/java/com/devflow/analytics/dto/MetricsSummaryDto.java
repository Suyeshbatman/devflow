package com.devflow.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

// The complete metrics snapshot shown on the dashboard
// All values come from Redis — sub-millisecond reads
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsSummaryDto {

    // Total orders ever placed
    private Long totalOrders;

    // Total revenue across all orders
    private BigDecimal totalRevenue;

    // Orders placed today
    private Long ordersToday;

    // Revenue generated today
    private BigDecimal revenueToday;

    // Top 5 best-selling products
    // Map<productName, orderCount>
    private Map<String, Integer> topProducts;

    // Orders per hour for the last 24 hours
    // Map<"2025-05-15T14", orderCount>
    private Map<String, Integer> ordersByHour;

    // Active users (unique buyers) today
    private Long activeUsersToday;
}