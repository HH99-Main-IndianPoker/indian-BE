package com.service.indianfrog.domain.chat.controller;

import com.service.indianfrog.domain.chat.entity.ChatMessage;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ChatController {

    private SimpMessageSendingOperations messagingTemplate; //이 심플메세지프로토콜을 이용하면 목적지와,메세지를 지정할수 있음!!
    private GameRoomService gameRoomService;

    public ChatController(SimpMessageSendingOperations messagingTemplate, GameRoomService gameRoomService) {
        this.messagingTemplate = messagingTemplate;
        this.gameRoomService = gameRoomService;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage) {

        if (!gameRoomService.existsById(roomId)) {
            // 이거 방이 없어도 없는 게임방번호 입력하면 채팅이 열리는 문제가 발생해서 검증로직 추가한거.
            // 게임방 폭파하면 채팅방도 없어지기에 굳이 필요없을것 같은데 혹시나 한번 더 걸어둠.
            return; // 방이 존재하지 않으면 종료.
        }

        String filteredContent = gameRoomService.filterMessage(chatMessage.getContent());
        chatMessage.setContent(filteredContent);

        // 동적으로 메시지를 라우팅할 주소를 생성.
        String destination = "/topic/gameRoom/" + roomId;

        // 보통 convertAndSend와 convertAndSendToUser 이 2개를 쓰는데 우린 1:N 채팅이니까 특정 유저에게만 전송할 필요가 없음.
        // 아닌가? 전체 채팅방은 1:N 하더라도 게임방은 1:1인가? 근데 그렇게 구현하려면 너무 복잡해 지려나?
        // 여기서 문제... 게임방마다 채팅방이 열리도록 로직 구현... 근데 로비채팅은 게임방이 없는데 어떻게 하지?...
        // 따로 전체 채팅방용 로직을 구현해야 하나? 그럼 너무 비효율적 아닌가... 좀 더 고민&공부 필요...
        messagingTemplate.convertAndSend(destination, chatMessage);
    }


    @MessageMapping("/chat.addUser/{roomId}")
    public void addUser(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        if (!gameRoomService.existsById(roomId)) {
            return;
        }

        //에러난 이유 : 헤더에 방번호를 추가안해줘서 나갔을때 어느방에서 나갔는지 모르기에 퇴장 메세지 안떴음. 리스너도 수정했음.
        headerAccessor.getSessionAttributes().put("room_id", roomId);
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        String destination = "/topic/gameRoom/" + roomId;

        // 입장 메시지 전송.
        messagingTemplate.convertAndSend(destination, chatMessage);
    }

}
