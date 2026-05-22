package com.devflow.analytics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// @EnableWebSocketMessageBroker = enables WebSocket with
// STOMP protocol (Simple Text Oriented Messaging Protocol)
// STOMP adds pub/sub semantics on top of raw WebSocket
// Client subscribes to a topic, server publishes to it
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements
        WebSocketMessageBrokerConfigurer {

    // Configures the message broker
    // Message broker = routes messages between server and clients
    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {

        // /topic = prefix for server → client messages
        // Client subscribes to: /topic/analytics
        // Server sends to:      /topic/analytics
        registry.enableSimpleBroker("/topic");

        // /app = prefix for client → server messages
        // If client sends a message, it goes to /app/...
        registry.setApplicationDestinationPrefixes("/app");
    }

    // Registers the WebSocket endpoint
    // React frontend connects to: ws://localhost:8083/ws
    // withSockJS() = fallback to HTTP polling if WebSocket
    // is not available (old browsers, firewalls etc.)
    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}