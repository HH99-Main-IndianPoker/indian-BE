package com.example.socketpractice.domain.game.controller;

import com.example.socketpractice.domain.chat.entity.ChatMessage;
import com.example.socketpractice.domain.game.dto.GameDto;
import com.example.socketpractice.domain.game.entity.Game;
import com.example.socketpractice.domain.game.service.EndGameService;
import com.example.socketpractice.domain.game.service.GameRoomService;
import com.example.socketpractice.domain.game.service.GameService;
import com.example.socketpractice.domain.game.service.StartGameService;
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
    private final GameService gameService;
    private final StartGameService startGameService;
    private final EndGameService endGameService;
    public GameController(SimpMessageSendingOperations messagingTemplate, GameService gameService, StartGameService startGameService, EndGameService endGameService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.startGameService = startGameService;
        this.endGameService = endGameService;
    }

    @MessageMapping("/{gameRoomId}/{gameState}")
    public ResponseEntity<?> handleGameState(@DestinationVariable Long gameRoomId, @DestinationVariable String gameState, @Payload ChatMessage chatMessage) {
        switch (gameState) {
            case "START":
                startGameService.startRound(gameRoomId);
                break;
            case "ACTION":
                gameService.playerAction(gameRoomId, chatMessage.getSender(), chatMessage.getContent());
                break;
            case "END":
                endGameService.endRound(gameRoomId);
                break;
            case "GAME_END":
                endGameService.endGame(gameRoomId);
                break;
        }

        /* 게임 상태 업데이트 메시지를 클라이언트에 전송 */
        String destination = "/topic/gameRoom/" + gameRoomId;
        messagingTemplate.convertAndSend(destination, chatMessage);

        return null;
    }
}
