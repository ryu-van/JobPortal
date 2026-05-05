package com.example.jobportal.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the Spring Security principal on the STOMP session from the
 * authentication stored by WebSocketAuthInterceptor during the HTTP handshake.
 * This is required for convertAndSendToUser() to route messages correctly.
 */
@Slf4j
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Object auth = sessionAttributes.get("user");
                if (auth instanceof UsernamePasswordAuthenticationToken token) {
                    accessor.setUser(token);
                    log.debug("STOMP CONNECT: principal set to {}", token.getName());
                } else {
                    log.warn("STOMP CONNECT: no valid auth in session attributes");
                }
            }
        }

        return message;
    }
}
