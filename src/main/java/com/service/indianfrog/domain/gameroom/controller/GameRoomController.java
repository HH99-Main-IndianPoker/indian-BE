package com.service.indianfrog.domain.gameroom.controller;

import com.service.indianfrog.domain.gameroom.dto.GameRoomDto;
import com.service.indianfrog.domain.gameroom.dto.ValidateRoomDto;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
    public ResponseEntity<Page<GameRoomDto>> getAllGameRooms(@PageableDefault(size = 15) Pageable pageable) {
        Page<GameRoomDto> gameRooms = gameRoomService.getAllGameRooms(pageable);
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
    public ResponseEntity<GameRoomDto> createGameRoom(@RequestBody GameRoomDto gameRoomDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String email = principal.getName();
        GameRoomDto createdGameRoom = gameRoomService.createGameRoom(gameRoomDto, email);
        return ResponseEntity.ok(createdGameRoom);
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<?> deleteGameRoom(@PathVariable Long roomId) {
        gameRoomService.deleteGameRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/game.join/{roomId}")
    public void joinGame(@DestinationVariable Long roomId, Principal principal) {
        if (principal == null) {
            logger.error("Principal is null. User is not authenticated.");
            return;
        }
        String email = principal.getName();
        ValidateRoomDto newParticipant = gameRoomService.addParticipant(roomId, email);
        messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId + "/join", newParticipant);
    }

    @MessageMapping("/game.leave/{roomId}")
    public void leaveGame(@DestinationVariable Long roomId, Principal principal) {
        if (principal == null) {
            logger.error("Principal is null. User is not authenticated.");
            return;
        }
        String email = principal.getName();
        gameRoomService.removeParticipant(roomId, email);
        messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId + "/leave", email);
    }
}
