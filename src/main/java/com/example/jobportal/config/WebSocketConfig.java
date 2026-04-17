package com.example.jobportal.config;

import com.example.jobportal.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;


@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final CorsProperties corsProperties;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        var endpoint = registry.addEndpoint("/ws");
        
        if (isProduction()) {
            corsProperties.getAllowedOrigins().forEach(endpoint::setAllowedOrigins);
        } else {
            endpoint.setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
        }
        
        endpoint.addInterceptors(webSocketAuthInterceptor);
        
        endpoint.withSockJS()
                .setHeartbeatTime(25000)
                .setDisconnectDelay(5000);
        
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(10)
                .queueCapacity(100);
    }

    private boolean isProduction() {
        return "prod".equals(activeProfile);
    }
}
