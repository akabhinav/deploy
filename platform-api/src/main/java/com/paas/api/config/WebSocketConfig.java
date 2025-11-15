package com.paas.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker
        config.enableSimpleBroker("/topic", "/queue");
        // Application destination prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint for logs streaming
        registry.addEndpoint("/ws/logs")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Register WebSocket endpoint for deployment updates
        registry.addEndpoint("/ws/deployments")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Register WebSocket endpoint for build logs
        registry.addEndpoint("/ws/builds")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
