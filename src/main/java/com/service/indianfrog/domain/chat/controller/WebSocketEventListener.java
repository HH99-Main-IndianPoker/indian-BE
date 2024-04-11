package com.service.indianfrog.domain.chat.controller;

import com.service.indianfrog.domain.chat.entity.ChatMessage;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
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

    @Autowired
    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, GameRoomService gameRoomService) {
        this.messagingTemplate = messagingTemplate;
        this.gameRoomService = gameRoomService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("새로운 웹소켓 연결!!");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // chat 관련 처리
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("room_id");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if(username != null && roomId != null) {
            logger.info("연결해제 : 유저네임 - " + username + ", 방번호 - " + roomId);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId, chatMessage);
        }

        // gameRoom 관련 처리
        String sessionId = headerAccessor.getSessionId();
        gameRoomService.removeParticipantBySessionId(sessionId);
    }
}