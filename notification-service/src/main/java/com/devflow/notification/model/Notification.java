package com.devflow.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Represents a single notification for a user
// Stored in Redis as JSON
// NOT stored in PostgreSQL — notifications are
// ephemeral (short-lived), Redis is perfect for this
// We keep last 50 notifications per user in Redis
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    // Unique ID for this notification
    private String id;

    // Who this notification is for
    private String userEmail;

    // Type of notification
    private NotificationType type;

    // Short title shown in notification bell
    // e.g. "Order Confirmed!"
    private String title;

    // Full message body
    // e.g. "Your order #42 for API Toolkit has been confirmed"
    private String message;

    // Has the user read this notification?
    @Builder.Default
    private boolean read = false;

    // Related entity ID (orderId, productId etc.)
    private String referenceId;

    private LocalDateTime createdAt;
}