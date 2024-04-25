package com.service.indianfrog.domain.chat.controller;

import com.service.indianfrog.domain.chat.entity.ChatMessage;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.utils.RepositoryHolder;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import com.service.indianfrog.domain.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final GameRoomService gameRoomService;
    /**
     * 웹소켓 연결 이벤트 핸들러
     */
    @Autowired
    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, GameRoomService gameRoomService) {
        this.messagingTemplate = messagingTemplate;
        this.gameRoomService = gameRoomService;
    }

    /**
     * 웹소켓 연결 이벤트 핸들러
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("새로운 웹소켓 연결!!");
    }

    /**
     * 웹소켓 연결 해제 이벤트 핸들러
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 연결 해제 이벤트와 관련된 메시지의 헤더에서 세션 속성을 추출.
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // chat 관련 처리
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("room_id");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null && roomId != null) {
            logger.info("연결해제 : 유저네임 - " + username + ", 방번호 - " + roomId);

            ChatMessage chatMessage = ChatMessage.builder()
                    .type(ChatMessage.MessageType.LEAVE)
                    .sender(username)
                    .build();

            messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId, chatMessage);
        }

        // 게임방의 참가자 목록에서 해당 사용자를 제거하여 게임방 상태를 최신 상태로 유지.
        String sessionId = headerAccessor.getSessionId();
        gameRoomService.removeParticipantBySessionId(sessionId);
    }
}