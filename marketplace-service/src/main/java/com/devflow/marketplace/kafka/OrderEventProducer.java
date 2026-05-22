package com.devflow.marketplace.kafka;

import com.devflow.marketplace.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

// @Component = Spring manages this bean
// KafkaTemplate = Spring's main class for sending Kafka messages
// Think of it like JdbcTemplate for databases but for Kafka
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    // KafkaTemplate<String, OrderEvent>
    // String = the type of the message KEY
    //   (we use orderId as key — ensures same order's
    //    events always go to same Kafka partition)
    // OrderEvent = the type of the message VALUE
    //   (the actual payload we're sending)
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    // Topic name = "order-events"
    // A topic is like a named channel in Kafka
    // Producers write to it, consumers read from it
    private static final String ORDER_TOPIC = "order-events";

    // Publishes an order event to the Kafka topic
    // This is ASYNCHRONOUS — we don't wait for consumers
    // to process it. We just fire and move on.
    public void publishOrderEvent(OrderEvent event) {
        log.info("Publishing order event: {} for order: {}",
                event.getEventType(), event.getOrderId());

        // kafkaTemplate.send() returns a CompletableFuture
        // CompletableFuture = Java's way of handling async operations
        // We attach callbacks to handle success and failure
        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(
                        ORDER_TOPIC,
                        // Key = orderId as string
                        // All events for the same order go to
                        // the same partition (ordering guaranteed)
                        String.valueOf(event.getOrderId()),
                        event
                );

        // whenComplete = runs when Kafka confirms receipt
        // result = success info (partition, offset)
        // exception = null if success, error if failed
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Order event published successfully. " +
                                "Topic: {}, Partition: {}, Offset: {}",
                        ORDER_TOPIC,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                // Log the error but don't crash the order flow
                // In production we'd save to a retry queue
                log.error("Failed to publish order event for order: {}. Error: {}",
                        event.getOrderId(), exception.getMessage());
            }
        });
    }
}