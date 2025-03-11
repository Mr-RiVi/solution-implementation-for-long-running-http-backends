package com.si4lr_http_b.bff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry configRegistry) {
        WebSocketMessageBrokerConfigurer.super.configureMessageBroker(configRegistry);
        configRegistry.enableSimpleBroker("/topic");
        configRegistry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Ensure all origins are allowed
                .withSockJS();  // Enable fallback for older browsers

        registry.addEndpoint("/ws-no-sockjs")  // Add a plain WebSocket endpoint
                .setAllowedOriginPatterns("*");
    }

}
