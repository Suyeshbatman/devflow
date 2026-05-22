package com.devflow.notification.controller;

import com.devflow.common.dto.ApiResponse;
import com.devflow.notification.dto.NotificationDto;
import com.devflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for current user
    // X-User-Email injected by API Gateway
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>>
    getNotifications(
            @RequestHeader("X-User-Email") String userEmail) {

        List<NotificationDto> notifications =
                notificationService.getNotifications(userEmail);
        return ResponseEntity.ok(ApiResponse.success(
                "Notifications retrieved", notifications));
    }

    // Get unread count — used for bell badge in navbar
    // Returns: { "count": 3 }
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>>
    getUnreadCount(
            @RequestHeader("X-User-Email") String userEmail) {

        long count = notificationService.getUnreadCount(userEmail);
        return ResponseEntity.ok(ApiResponse.success(
                "Unread count retrieved",
                Map.of("count", count)));
    }

    // Mark a notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id,
            @RequestHeader("X-User-Email") String userEmail) {

        notificationService.markAsRead(id, userEmail);
        return ResponseEntity.ok(
                ApiResponse.success("Notification marked as read"));
    }
}