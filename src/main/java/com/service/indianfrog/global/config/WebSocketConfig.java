package com.service.indianfrog.global.config;

import com.service.indianfrog.global.jwt.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    private JwtUtil jwtUtil;

    public WebSocketConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 웹소켓 연결을 위한 엔드포인트를 등록
     * 클라이언트는 이 엔드포인트를 통해 웹소켓 서버에 연결할 수 있음
     * SockJS 프로토콜을 사용하여 웹소켓을 지원하지 않는 브라우저에서도 연결을 지원
     *
     * @param registry 스톰프 엔드포인트를 등록할 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //스톰프로 엔트포인트 설정
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

    }

    /**
     * 메시지 브로커 구성
     *
     * @param registry 메시지 브로커 설정을 위한 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic으로 시작하는 메시지에 대한 브로커를 활성화
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
        // 클라이언트에서 메시지를 전송할 때 /app으로 시작하는 경로로 라우팅
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 클라이언트로부터 들어오는 메시지가 웹소켓 채널을 통해 서버에 도달하기 전에 인터셉터를 구성
     * 이 인터셉터는 JWT를 사용하여 들어오는 메시지의 인증 정보를 검사
     *
     * @param registration 인바운드 채널 설정을 위한 등록 객체
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JWT를 사용한 인증 인터셉터
        registration.interceptors(new WebSocketAuthChannelInterceptor(jwtUtil));
    }
}