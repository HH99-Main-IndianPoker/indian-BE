package com.service.indianfrog.domain.gameroom.controller;

import com.service.indianfrog.domain.gameroom.dto.GameRoomDto;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import com.service.indianfrog.global.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/gameRoom")
public class GameRoomController {

    private static final Logger logger = LoggerFactory.getLogger(GameRoomController.class);
    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameRoomController(GameRoomService gameRoomService, SimpMessagingTemplate messagingTemplate) {
        this.gameRoomService = gameRoomService;
        this.messagingTemplate = messagingTemplate;
    }


    @GetMapping("/")
    public ResponseEntity<List<GameRoomDto>> getAllGameRooms() {
        List<GameRoomDto> gameRooms = gameRoomService.getAllGameRooms();
        return ResponseEntity.ok(gameRooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<GameRoomDto> getGameRoomById(@PathVariable Long roomId) {
        GameRoomDto gameRoom = gameRoomService.getGameRoomById(roomId);
        if (gameRoom != null) {
            return ResponseEntity.ok(gameRoom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping("/create")
    public ResponseEntity<GameRoomDto> createGameRoom(@RequestBody GameRoomDto gameRoomDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            // 사용자가 로그인하지 않은 경우 접근을 거부합니다.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // 게임방 생성 로직을 호출합니다.
        GameRoomDto createdGameRoom = gameRoomService.createGameRoom(gameRoomDto);
        // 생성된 게임방 정보를 반환합니다.
        return ResponseEntity.ok(createdGameRoom);
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<?> deleteGameRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            // 사용자가 로그인하지 않은 경우 접근을 거부합니다.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // 게임방 삭제 로직을 호출합니다.
        gameRoomService.deleteGameRoom(roomId);
        // 성공적으로 삭제되었음을 나타내는 응답을 반환합니다.
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
