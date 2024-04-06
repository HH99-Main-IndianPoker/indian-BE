package com.service.indianfrog.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //스톰프로 엔트포인트 설정해주고
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // allowedOrigins 하니까 안돼서 바꿈.
                .withSockJS();

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic으로 시작하는 메시지에 대한 브로커를 활성화
        registry.enableSimpleBroker("/topic");
        // 클라이언트에서 메시지를 전송할 때 /app으로 시작하는 경로로 라우팅
        registry.setApplicationDestinationPrefixes("/app");
    }
}