package com.service.indianfrog.domain.chat.controller;

import com.service.indianfrog.domain.chat.entity.ChatMessage;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class ChatController {

    private SimpMessageSendingOperations messagingTemplate; //이 심플메세지프로토콜을 이용하면 목적지와,메세지를 지정할수 있음
    private GameRoomService gameRoomService;
    private UserRepository userRepository;

    public ChatController(SimpMessageSendingOperations messagingTemplate, GameRoomService gameRoomService,UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.gameRoomService = gameRoomService;
        this.userRepository = userRepository;
    }

    /**
     * 채팅 메시지를 전송하는 메서드
     *
     * @param gameRoomId  게임방 ID
     * @param chatMessage 채팅 메시지
     */
    @MessageMapping("/chat.sendMessage/{gameRoomId}")
    public void sendMessage(@DestinationVariable Long gameRoomId, @Payload ChatMessage chatMessage) {

        if (!gameRoomService.existsById(gameRoomId)) {
            return; // 방이 존재하지 않으면 종료.
        }

        // 욕설 필터링
        String filteredContent = gameRoomService.filterMessage(chatMessage.getContent());
        chatMessage.setContent(filteredContent);

        // 동적으로 메시지를 라우팅할 주소를 생성.
        String destination = "/topic/gameRoom/" + gameRoomId;

        messagingTemplate.convertAndSend(destination, chatMessage);
    }

    /**
     * 사용자를 채팅방에 추가하는 메서드
     *
     * @param gameRoomId     게임방 ID
     * @param chatMessage    채팅 메시지
     * @param headerAccessor 메시지 헤더 접근자
     */
    @MessageMapping("/chat.addUser/{gameRoomId}")
    public void addUser(@DestinationVariable Long gameRoomId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor, Principal principal) {

        if (!gameRoomService.existsById(gameRoomId)) {
            return;
        }

        // 헤더에 방번호를 추가안해주면 나갔을때 어느방에서 나갔는지 모르기에 퇴장 메세지를 위해 추가.
        headerAccessor.getSessionAttributes().put("room_id", gameRoomId);
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        String destination = "/topic/gameRoom/" + gameRoomId;

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

//        int point = user.getPoints();
        chatMessage.setPoint(user.getPoints());
        log.info("내포인트는?"+chatMessage.getPoint());


        // 입장 메시지 전송.
        messagingTemplate.convertAndSend(destination, chatMessage);
//        messagingTemplate.convertAndSend(destination, point);
    }

//    @MessageMapping("/gameRoom/{gameRoomId}/join")
//    public void userPoint(@DestinationVariable Long gameRoomId, Principal principal) {
//        String email = principal.getName();
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
//
//        int point = user.getPoints();
//        log.info("내포인트는?"+point);
//        String destination = "/topic/gameRoom/" + gameRoomId;
//
//        messagingTemplate.convertAndSend(destination, point);
//    }

//    @GetMapping("/gameRoom/point") 테스트용
//    public ResponseEntity<Integer> getUserPoint(Principal principal) {
//        String email = principal.getName();
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
//
//        int point = user.getPoints();
//        log.info("내 포인트는? " + point);
//        return ResponseEntity.ok(point);
//    }
}
