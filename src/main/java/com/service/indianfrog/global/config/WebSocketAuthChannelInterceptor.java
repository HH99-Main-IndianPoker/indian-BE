package com.service.indianfrog.global.config;

import com.service.indianfrog.global.jwt.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.messaging.support.ChannelInterceptor;

public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith(JwtUtil.BEARER_PREFIX)) {
            token = token.substring(7);
            if (jwtUtil.verifyToken(token)) {
                String email = jwtUtil.getUid(token);
                String role = jwtUtil.getRole(token);
                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null); // Create an authentication object based on the user's details
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        return message;
    }
}
