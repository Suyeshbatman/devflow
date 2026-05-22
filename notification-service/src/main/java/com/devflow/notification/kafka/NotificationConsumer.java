package com.devflow.notification.kafka;

import com.devflow.notification.dto.OrderEventDto;
import com.devflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    // Same topic as analytics-service ("order-events")
    // BUT different groupId ("notification-group")
    //
    // This is KEY to understanding Kafka consumer groups:
    //
    // analytics-group receives: ORDER_PLACED event
    //   → updates metrics in Redis
    //
    // notification-group ALSO receives: SAME ORDER_PLACED event
    //   → sends notification to user
    //
    // Kafka delivers the SAME message to EVERY consumer group
    // Each group processes it independently
    // Neither group affects the other
    @KafkaListener(
            topics = "order-events",
            groupId = "notification-group"
    )
    public void consumeOrderEvent(
            @Payload OrderEventDto event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Notification consumer received event: {} " +
                        "partition: {} offset: {}",
                event.getEventType(), partition, offset);

        try {
            notificationService.processOrderEvent(event);
        } catch (Exception e) {
            log.error("Failed to process notification for event: {}",
                    e.getMessage(), e);
        }
    }
}