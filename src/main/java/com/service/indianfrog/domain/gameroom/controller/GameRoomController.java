package com.service.indianfrog.domain.gameroom.controller;

import com.service.indianfrog.domain.gameroom.dto.GameRoomRequestDto.GameRoomCreateRequestDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GameRoomCreateResponseDto;
import com.service.indianfrog.domain.gameroom.dto.ValidateRoomDto;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import com.service.indianfrog.global.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.*;

@Slf4j
@RestController
@RequestMapping("/gameRoom")
public class GameRoomController {

    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameRoomController(GameRoomService gameRoomService, SimpMessagingTemplate messagingTemplate) {
        this.gameRoomService = gameRoomService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/")
    public ResponseDto<Page<GetGameRoomResponseDto>> getAllGameRooms(@PageableDefault(size = 15) Pageable pageable) {
        Page<GetGameRoomResponseDto> gameRooms = gameRoomService.getAllGameRooms(pageable);
        return ResponseDto.success("모든 게임방 조회 기능",gameRooms);
    }

    @GetMapping("/{roomId}")
    public ResponseDto<GetGameRoomResponseDto> getGameRoomById(@PathVariable Long roomId) {
        GetGameRoomResponseDto gameRoom = gameRoomService.getGameRoomById(roomId);
        return ResponseDto.success("게임방건단  조회 기능",gameRoom);
    }

    @PostMapping("/create")
    public ResponseDto<GameRoomCreateResponseDto> createGameRoom(@RequestBody GameRoomCreateRequestDto gameRoomDto, Principal principal) {
        GameRoomCreateResponseDto gameRoom = gameRoomService.createGameRoom(gameRoomDto, principal);
        return ResponseDto.success("게임방 생성 기능", gameRoom);
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<?> deleteGameRoom(@PathVariable Long roomId) {
        gameRoomService.deleteGameRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/game.join/{roomId}")
    public void joinGame(@DestinationVariable Long roomId, Principal principal) {
        ValidateRoomDto newParticipant = gameRoomService.addParticipant(roomId, principal);
        messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId + "/join", newParticipant);
    }

    @MessageMapping("/game.leave/{roomId}")
    public void leaveGame(@DestinationVariable Long roomId, Principal principal) {
        gameRoomService.removeParticipant(roomId, principal);
        messagingTemplate.convertAndSend("/topic/gameRoom/" + roomId + "/leave", principal.getName());
    }
}
