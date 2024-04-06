package com.service.indianfrog.domain.game.controller;

import com.service.indianfrog.domain.chat.entity.ChatMessage;
import com.service.indianfrog.domain.game.dto.UserChoices;
import com.service.indianfrog.domain.game.service.EndGameService;
import com.service.indianfrog.domain.game.service.GamePlayService;
import com.service.indianfrog.domain.game.service.GameSessionService;
import com.service.indianfrog.domain.game.service.StartGameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.ErrorMessage;
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
        Object response = switch (gameState) {
            case "START" -> startGameService.startRound(gameRoomId);
            case "ACTION" ->
                    gamePlayService.playerAction(gameRoomId, chatMessage.getSender(), chatMessage.getContent());
            case "END" -> endGameService.endRound(gameRoomId);
            case "GAME_END" -> endGameService.endGame(gameRoomId);
            case "USER_CHOICE" -> gameSessionService.processUserChoices(userChoices);
            default -> new ErrorMessage("올바른 게임 상태가 아닙니다");
        };

        /* 게임 상태 업데이트 메시지를 클라이언트에 전송 */
        String destination = "/topic/gameRoom/" + gameRoomId;
        messagingTemplate.convertAndSend(destination, response);
    }
}
