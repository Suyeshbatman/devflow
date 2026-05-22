package com.devflow.notification.dto;

import com.devflow.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Safe DTO returned to clients via REST/WebSocket
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private String referenceId;
    private LocalDateTime createdAt;
}