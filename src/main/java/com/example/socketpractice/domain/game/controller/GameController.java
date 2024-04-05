package com.example.socketpractice.domain.game.controller;

import com.example.socketpractice.domain.chat.entity.ChatMessage;
import com.example.socketpractice.domain.game.dto.GameDto;
import com.example.socketpractice.domain.game.dto.UserChoices;
import com.example.socketpractice.domain.game.entity.Game;
import com.example.socketpractice.domain.game.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "게임 실행 컨트롤러", description = "인디언 포커 게임 실행 및 종료 컨트롤러입니다.")
@Slf4j
@Controller
@RequestMapping("/gameRoom")
public class GameController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final StartGameService startGameService;
    private final GamePlayService gamePlayService;
    private final EndGameService endGameService;
    private final GameSessionService gameSessionService;
    public GameController(SimpMessageSendingOperations messagingTemplate,
                          StartGameService startGameService, GamePlayService gamePlayService,
                          EndGameService endGameService, GameSessionService gameSessionService) {
        this.messagingTemplate = messagingTemplate;
        this.startGameService = startGameService;
        this.gamePlayService = gamePlayService;
        this.endGameService = endGameService;
        this.gameSessionService = gameSessionService;
    }

    @MessageMapping("/{gameRoomId}/{gameState}")
    public void handleGameState(@DestinationVariable Long gameRoomId, @DestinationVariable String gameState,
                                             @Payload ChatMessage chatMessage, @Payload(required = false) UserChoices userChoices) {
        switch (gameState) {
            case "START":
                startGameService.startRound(gameRoomId);
                break;
            case "ACTION":
                gamePlayService.playerAction(gameRoomId, chatMessage.getSender(), chatMessage.getContent());
                break;
            case "END":
                endGameService.endRound(gameRoomId);
                break;
            case "GAME_END":
                endGameService.endGame(gameRoomId);
                break;
            case "USER_CHOICE":
                gameSessionService.processUserChoices(userChoices);
                break;
        }

        /* 게임 상태 업데이트 메시지를 클라이언트에 전송 */
        String destination = "/topic/gameRoom/" + gameRoomId;
        messagingTemplate.convertAndSend(destination, chatMessage);
    }
}
