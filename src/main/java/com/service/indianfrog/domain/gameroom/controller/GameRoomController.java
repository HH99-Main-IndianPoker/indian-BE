package com.service.indianfrog.domain.gameroom.controller;

import com.service.indianfrog.domain.gameroom.dto.GameRoomDto;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // CORS
@RestController
@RequestMapping("/gameRoom")
public class GameRoomController {

    private GameRoomService gameRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameRoomController(GameRoomService gameRoomService, SimpMessagingTemplate messagingTemplate) {
        this.gameRoomService = gameRoomService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<GameRoomDto> createGameRoom(@RequestBody GameRoomDto gameRoomDto) {
        GameRoomDto createdGameRoom = gameRoomService.createGameRoom(gameRoomDto);
        // GameRoomDto createdGameRoom = gameRoomService.createGameRoom(gameRoomDto, creatorParticipant);  @RequestParam String creatorParticipant
        return ResponseEntity.ok(createdGameRoom);
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<?> deleteGameRoom(@PathVariable Long roomId) {
        gameRoomService.deleteGameRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/game.join/{roomId}")
    public void joinGame(@DestinationVariable Long roomId, String participant) {
        try {
            GameRoomDto gameRoom = gameRoomService.addParticipant(roomId, participant);
            messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId, gameRoom.getParticipants());
        } catch (IllegalStateException e) {
            // 게임방이 꽉 찼을 때의 예외 처리
            // 적절한 예외 처리 로직 추가 예정
        }
    }

    @MessageMapping("/game.leave/{roomId}")
    public void leaveGame(@DestinationVariable Long roomId, String participant) {
        GameRoomDto gameRoom = gameRoomService.removeParticipant(roomId, participant);
        if (gameRoom != null) {
            messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId, gameRoom.getParticipants());
        }
    }
}
