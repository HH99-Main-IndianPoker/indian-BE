package com.service.indianfrog.domain.gameroom.controller;

import com.service.indianfrog.domain.gameroom.dto.GameRoomRequestDto.GameRoomCreateRequestDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GameRoomCreateResponseDto;
import com.service.indianfrog.domain.gameroom.dto.ParticipantInfo;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    /**
     * 모든 게임 방을 페이징하여 조회
     * @param pageable 페이징 정보
     * @return 조회된 게임 방의 페이징된 목록
     */
    @GetMapping("") // 전체페이지 조회
    public ResponseDto<Page<GetGameRoomResponseDto>> getAllGameRooms(@PageableDefault(size = 15) Pageable pageable) {
        Page<GetGameRoomResponseDto> gameRooms = gameRoomService.getAllGameRooms(pageable);
        return ResponseDto.success("모든 게임방 조회 기능",gameRooms);
    }

    /**
     * 주어진 ID를 가진 게임 방을 조회
     * @param gameRoomId 게임 방 ID
     * @return 조회된 게임 방 정보
     */
    @GetMapping("/{gameRoomId}") //특정 게임방 조회
    public ResponseDto<GetGameRoomResponseDto> getGameRoomById(@PathVariable Long gameRoomId) {
        GetGameRoomResponseDto gameRoom = gameRoomService.getGameRoomById(gameRoomId);
        return ResponseDto.success("게임방 조회 기능",gameRoom);
    }

    /**
     * 새로운 게임 방을 생성
     * @param gameRoomDto 게임 방 생성 요청 Dto
     * @param principal 요청을 보낸 사용자의 정보
     * @return 생성된 게임 방 정보
     */
    @PostMapping("/create") //채널인터셉터로 인증된 사용자의 인증정보 사용하여 방생성
    public ResponseDto<GameRoomCreateResponseDto> createGameRoom(@RequestBody GameRoomCreateRequestDto gameRoomDto, Principal principal) {
        GameRoomCreateResponseDto gameRoom = gameRoomService.createGameRoom(gameRoomDto, principal);
        return ResponseDto.success("게임방 생성 기능", gameRoom);
    }

    /**
     * 주어진 ID를 가진 게임 방을 삭제
     * @param gameRoomId 게임방 ID
     * @return 응답 엔티티 (성공 시 HTTP 200)
     */
    @DeleteMapping("/delete/{gameRoomId}") // 게임방 삭제
    public ResponseEntity<?> deleteGameRoom(@PathVariable Long gameRoomId) {
        gameRoomService.deleteGameRoom(gameRoomId);
        return ResponseEntity.ok().build();
    }

    /**
     * 게임 방에 참가
     * @param gameRoomId 게임방 ID
     * @param userDetails 참가자 정보
     */
    @PostMapping("/{gameRoomId}/join")
    public ResponseEntity<Object> joinGameRoom(@PathVariable Long gameRoomId, @AuthenticationPrincipal UserDetails userDetails) {
        ParticipantInfo newParticipant = gameRoomService.addParticipant(gameRoomId, userDetails);
        messagingTemplate.convertAndSend("/topic/gameRoom/" + gameRoomId + "/join", newParticipant);
        return ResponseEntity.ok().build();
    }

    /**
     * 게임 방에서 나갸기
     * @param gameRoomId 게임방 ID
     * @param principal 참가자 정보
     */
    @MessageMapping("/{gameRoomId}/leave")
    public void leaveGame(@DestinationVariable Long gameRoomId, Principal principal) {
        //removeParticipant 메써드를 호출해서 게임방에서 principal로 받아온 사용자를 제거
        gameRoomService.removeParticipant(gameRoomId, principal);
        messagingTemplate.convertAndSend("/topic/gameRoom/" + gameRoomId + "/leave", principal.getName());
    }
}
