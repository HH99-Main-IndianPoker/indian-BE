package com.service.indianfrog.global.config;

import com.service.indianfrog.domain.gameroom.dto.AuthenticatedUser;
import com.service.indianfrog.global.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;


    /**
     * JwtUtil 객체를 주입받아 인터셉터를 생성
     *
     * @param jwtUtil JWT 처리
     */
    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 메시지 전송 전에 인증 정보를 검사하고 설정
     *
     * @param message 전송될 메시지
     * @param channel 메시지가 전송될 채널
     * @return 인증 처리 후의 메시지 객체
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
        // 클라이언트가 보낸 메세지에서 Authorization 추출.
        // getFirstNativeHeader는 STOMP를 사용하는 웹소켓에서 헤더에 접근할수 있게 해주는 프로토콜
        String token = accessor.getFirstNativeHeader("Authorization");

        // 토큰 유효성 검사
        if (StringUtils.hasText(token) && token.startsWith(JwtUtil.BEARER_PREFIX)) {
            token = token.substring(7);

            // jwt로 유효성 검사
            if (jwtUtil.verifyToken(token)) {
                String email = jwtUtil.getUid(token);

                // 이메일을 이용해서 인증객체 생성. new AuthenticatedUser(email) 이걸로 principal 사용.
                Authentication authentication = new UsernamePasswordAuthenticationToken(new AuthenticatedUser(email), null, Collections.emptyList());
                //SecurityContext에 담아서 다른데서도 인증된 사용자 정보를 조회할수 있음.
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // STOMP에 사용자 인증정보 설정해서 해당 메세지 처리하는 동안 사용자가 인증된 상태 유지.
                accessor.setUser(authentication);
                log.info("Authentication set for user: {}", email);
            }
        }
        return message;
    }
}
