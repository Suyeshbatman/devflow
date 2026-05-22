package com.devflow.notification.model;

// Types of notifications the platform can send
public enum NotificationType {
    ORDER_CONFIRMED,    // buyer placed an order successfully
    ORDER_CANCELLED,    // order was cancelled
    NEW_PRODUCT,        // new product listed in marketplace
    SYSTEM_ALERT,       // platform-wide announcements
    ACHIEVEMENT         // user earned a badge/milestone
}