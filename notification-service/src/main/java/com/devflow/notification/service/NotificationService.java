package com.devflow.notification.service;

import com.devflow.notification.dto.NotificationDto;
import com.devflow.notification.dto.OrderEventDto;
import com.devflow.notification.model.Notification;
import com.devflow.notification.model.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Redis key pattern for user notifications
    // notifications:john@devflow.com → List of Notification JSON
    private static final String NOTIFICATIONS_KEY =
            "notifications:";

    // Max notifications stored per user in Redis
    // Oldest ones dropped when limit reached
    private static final long MAX_NOTIFICATIONS = 50;

    // Called by Kafka consumer when order event arrives
    public void processOrderEvent(OrderEventDto event) {
        log.info("Processing notification for event: {}",
                event.getEventType());

        if ("ORDER_PLACED".equals(event.getEventType())) {
            sendOrderConfirmation(event);
        } else if ("ORDER_CANCELLED".equals(event.getEventType())) {
            sendOrderCancellation(event);
        }
    }

    private void sendOrderConfirmation(OrderEventDto event) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .userEmail(event.getBuyerEmail())
                .type(NotificationType.ORDER_CONFIRMED)
                .title("Order Confirmed!")
                .message(String.format(
                        "Your order #%d for '%s' has been confirmed. " +
                                "Amount paid: $%.2f",
                        event.getOrderId(),
                        event.getProductName(),
                        event.getPricePaid()))
                .read(false)
                .referenceId(String.valueOf(event.getOrderId()))
                .createdAt(LocalDateTime.now())
                .build();

        saveAndPush(notification);
    }

    private void sendOrderCancellation(OrderEventDto event) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .userEmail(event.getBuyerEmail())
                .type(NotificationType.ORDER_CANCELLED)
                .title("Order Cancelled")
                .message(String.format(
                        "Your order #%d for '%s' has been cancelled.",
                        event.getOrderId(),
                        event.getProductName()))
                .read(false)
                .referenceId(String.valueOf(event.getOrderId()))
                .createdAt(LocalDateTime.now())
                .build();

        saveAndPush(notification);
    }

    private void saveAndPush(Notification notification) {
        // Save to Redis list for this user
        // LPUSH = push to LEFT (front) of list
        // so newest notifications appear first
        String key = NOTIFICATIONS_KEY + notification.getUserEmail();
        redisTemplate.opsForList().leftPush(key, notification);

        // Trim list to MAX_NOTIFICATIONS
        // If user has 51 notifications, drop the oldest one
        redisTemplate.opsForList().trim(key, 0, MAX_NOTIFICATIONS - 1);

        log.info("Notification saved for user: {}",
                notification.getUserEmail());

        // Push via WebSocket to user's personal queue
        // /queue/notifications = user-specific channel
        // Only the user with this email receives it
        NotificationDto dto = mapToDto(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + notification.getUserEmail(),
                dto);

        log.info("Notification pushed via WebSocket to: {}",
                notification.getUserEmail());
    }

    // Get all notifications for a user (from Redis)
    public List<NotificationDto> getNotifications(String userEmail) {
        String key = NOTIFICATIONS_KEY + userEmail;

        // LRANGE = get items from Redis list
        // 0, -1 = from first to last (all items)
        List<Object> raw = redisTemplate.opsForList()
                .range(key, 0, -1);

        if (raw == null) return List.of();

        return raw.stream()
                .map(obj -> objectMapper.convertValue(
                        obj, Notification.class))
                .map(this::mapToDto)
                .toList();
    }

    // Mark a specific notification as read
    public void markAsRead(String notificationId, String userEmail) {
        String key = NOTIFICATIONS_KEY + userEmail;
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);

        if (raw == null) return;

        raw.stream()
                .map(obj -> objectMapper.convertValue(obj, Notification.class))
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .ifPresent(notification -> {
                    // Remove old version
                    redisTemplate.opsForList().remove(key, 1, notification);
                    // Save updated version with read=true
                    notification.setRead(true);
                    redisTemplate.opsForList().leftPush(key, notification);
                });
    }

    // Count unread notifications (for bell badge in navbar)
    public long getUnreadCount(String userEmail) {
        String key = NOTIFICATIONS_KEY + userEmail;
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);

        if (raw == null) return 0;

        return raw.stream()
                .map(obj -> objectMapper.convertValue(
                        obj, Notification.class))
                .filter(n -> !n.isRead())
                .count();
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}