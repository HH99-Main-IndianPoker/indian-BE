package com.service.indianfrog.domain.game.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.indianfrog.domain.game.dto.GameBetting;
import com.service.indianfrog.domain.game.dto.GameRequest;
import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.dto.UserChoices;
import com.service.indianfrog.domain.game.redis.RedisRequestManager;
import com.service.indianfrog.domain.game.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Tag(name = "게임 실행 컨트롤러", description = "인디언 포커 게임 실행 및 종료 컨트롤러입니다.")
@Slf4j
@Controller
public class GameController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ReadyService readyService;
    private final RedisRequestManager redisRequestManager;
    private final ObjectMapper objectMapper;

    public GameController(SimpMessageSendingOperations messagingTemplate, ReadyService readyService,
                          RedisRequestManager redisRequestManager, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.readyService = readyService;
        this.redisRequestManager = redisRequestManager;
        this.objectMapper = objectMapper;
    }


    /* pub 사용 게임 준비 */
    @MessageMapping("/gameRoom/{gameRoomId}/ready")
    public void gameReady(
            @DestinationVariable Long gameRoomId, Principal principal) {
        log.info("게임 준비 - 게임방 아이디 : {}", gameRoomId);
        GameStatus gameStatus = readyService.gameReady(gameRoomId, principal);
        String destination = "/topic/gameRoom/" + gameRoomId;
        messagingTemplate.convertAndSend(destination, gameStatus);
    }

    @MessageMapping("/gameRoom/{gameRoomId}/{gameState}")
    public void handleGameState(@DestinationVariable Long gameRoomId, @DestinationVariable String gameState,
                                @Payload(required = false) GameBetting gameBetting, @Payload(required = false) UserChoices userChoices, Principal principal) throws JsonProcessingException {

        /* 요청을 Redis Sorted Set에 저장*/
        String email = principal.getName();
        GameRequest request = new GameRequest(gameRoomId, gameState, email, gameBetting, userChoices);
        String requestJson = objectMapper.writeValueAsString(request);

        redisRequestManager.enqueueRequest(gameRoomId.toString(), requestJson);
        log.info("Request for gameState: {} in gameRoom: {} has been enqueued", gameState, gameRoomId);

        /* 요청을 순서대로 실행*/
        redisRequestManager.processRequests(gameRoomId.toString());
        log.info("processRequests 완료");
    }
}