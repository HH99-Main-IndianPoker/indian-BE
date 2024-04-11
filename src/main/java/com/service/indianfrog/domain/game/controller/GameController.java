package com.service.indianfrog.domain.game.controller;

import com.service.indianfrog.domain.game.dto.GameBetting;
import com.service.indianfrog.domain.game.dto.GameDto.StartRoundResponse;
import com.service.indianfrog.domain.game.dto.GameInfo;
import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.dto.UserChoices;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.GameState;
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
    private final StartGameService startGameService;
    private final GamePlayService gamePlayService;
    private final EndGameService endGameService;
    private final GameSessionService gameSessionService;
    private final ReadyService readyService;
    public GameController(SimpMessageSendingOperations messagingTemplate,
                          StartGameService startGameService, GamePlayService gamePlayService,
                          EndGameService endGameService, GameSessionService gameSessionService, ReadyService readyService) {
        this.messagingTemplate = messagingTemplate;
        this.startGameService = startGameService;
        this.gamePlayService = gamePlayService;
        this.endGameService = endGameService;
        this.gameSessionService = gameSessionService;
        this.readyService = readyService;
    }

    @MessageMapping("/gameRoom/{gameRoomId}/{gameState}")
    public void handleGameState(@DestinationVariable Long gameRoomId, @DestinationVariable String gameState,
                                @Payload(required = false) GameBetting gameBetting, @Payload(required = false) UserChoices userChoices) {

        switch (gameState) {
            case "START" -> {
                StartRoundResponse response = startGameService.startRound(gameRoomId);
                sendUserGameMessage(response); // 유저별 메시지 전송
            }
            case "ACTION", "END", "GAME_END", "USER_CHOICE"-> {
                Object response = switch (gameState) {
                    case "ACTION" ->
                            gamePlayService.playerAction(gameRoomId, gameBetting.getNickname(), gameBetting.getAction());
                    case "END" -> endGameService.endRound(gameRoomId);
                    case "GAME_END" -> endGameService.endGame(gameRoomId);
                    case "USER_CHOICE" -> gameSessionService.processUserChoices(userChoices);
                    default -> throw new IllegalStateException("Unexpected value: " + gameState);
                };
                // 공통 메시지 전송
                String destination = "/topic/gameRoom/" + gameRoomId;
                messagingTemplate.convertAndSend(destination, response);
            }
            default -> throw new IllegalStateException("Invalid game state: " + gameState);
        }
    }

    // /pub 사용 게임 준비
    @MessageMapping("/gameRoom/{gameRoomId}/ready")
    public void gameReady(
            @DestinationVariable Long gameRoomId, Principal principal) {
        log.info("게임 준비 - 게임방 아이디 : {}", gameRoomId);
        GameStatus gameStatus = readyService.gameReady(gameRoomId, principal);
        String destination = "/topic/gameRoom/" + gameRoomId;
        messagingTemplate.convertAndSend(destination, gameStatus);
    }

    private void sendUserGameMessage(StartRoundResponse response) {
        /* 각 Player 에게 상대 카드 정보와 턴 정보를 전송*/
        String playerOneId = response.getPlayerOneInfo().getId();
        Card playerTwoCard = response.getPlayerTwoInfo().getCard();
        messagingTemplate.convertAndSendToUser(
                playerOneId,
                "/queue/gameInfo",
                new GameInfo(playerTwoCard, response.getTurn())
        );

        String playerTwoId = response.getPlayerTwoInfo().getId();
        Card playerOneCard = response.getPlayerOneInfo().getCard();
        messagingTemplate.convertAndSendToUser(
                playerTwoId,
                "/queue/gameInfo",
                new GameInfo(playerOneCard, response.getTurn())
        );
    }
}