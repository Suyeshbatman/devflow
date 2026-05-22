package com.devflow.marketplace.model;

// Tracks the lifecycle of an order
// PENDING   → order placed, payment not confirmed
// CONFIRMED → payment successful
// CANCELLED → user or system cancelled the order
// REFUNDED  → money returned to buyer
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    REFUNDED
}