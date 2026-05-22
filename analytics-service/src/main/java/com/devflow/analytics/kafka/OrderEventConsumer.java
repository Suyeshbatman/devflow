package com.devflow.analytics.kafka;

import com.devflow.analytics.dto.OrderEventDto;
import com.devflow.analytics.service.MetricsService;
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
public class OrderEventConsumer {

    private final MetricsService metricsService;

    // @KafkaListener = marks this method as a Kafka consumer
    // topics = which topic to listen to
    //   must match what marketplace-service publishes to
    // groupId = consumer group (from application.yml)
    //
    // How Kafka delivers messages:
    // 1. marketplace publishes OrderEvent to "order-events"
    // 2. Kafka stores it in the topic
    // 3. This method is called automatically with the message
    // 4. We process it and update Redis metrics
    @KafkaListener(
            topics = "order-events",
            groupId = "analytics-group"
    )
    public void consumeOrderEvent(
            // @Payload = the actual message body
            @Payload OrderEventDto event,
            // @Header = Kafka metadata about the message
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received order event from partition: {}, offset: {}",
                partition, offset);
        log.info("Event: {} for order: {}",
                event.getEventType(), event.getOrderId());

        try {
            metricsService.processOrderEvent(event);
        } catch (Exception e) {
            // Log but don't rethrow — if we throw here
            // Kafka will retry the message endlessly
            // In production we'd send to a dead letter topic
            log.error("Failed to process order event: {}",
                    e.getMessage(), e);
        }
    }
}