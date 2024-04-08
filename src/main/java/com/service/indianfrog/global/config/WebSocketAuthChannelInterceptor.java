package com.service.indianfrog.global.config;

import com.service.indianfrog.domain.gameroom.dto.AuthenticatedUser;
import com.service.indianfrog.global.jwt.JwtUtil;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.Collections;

@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
        String token = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith(JwtUtil.BEARER_PREFIX)) {
            token = token.substring(7);
            if (jwtUtil.verifyToken(token)) {
                String email = jwtUtil.getUid(token);
                Authentication authentication = new UsernamePasswordAuthenticationToken(new AuthenticatedUser(email), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);
                log.info("Authentication set for user: {}", email);
            }
        }
        return message;
    }
}
