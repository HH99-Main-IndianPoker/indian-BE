package com.service.indianfrog.domain.game.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.indianfrog.domain.game.dto.*;
import com.service.indianfrog.domain.game.service.EndGameService;
import com.service.indianfrog.domain.game.service.GamePlayService;
import com.service.indianfrog.domain.game.service.GameSessionService;
import com.service.indianfrog.domain.game.service.StartGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class RedisRequestManager {
    /* 동시 요청을 관리하기 위한 요청 관리 클래스
    * Redis Sorted Set을 사용하여 요청에 대한 우선 순위를 설정하고 우선 순위에 따라 요청이 처리되도록 설정
    * GameController에 요청을 보내면 요청을 Sorted Set에 저장 후 우선 순위에 따라 각 게임 서비스 로직 호출*/

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final StartGameService startGameService;
    private final GamePlayService gamePlayService;
    private final EndGameService endGameService;
    private final GameSessionService gameSessionService;

    public RedisRequestManager(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper,
                               SimpMessageSendingOperations messagingTemplate, StartGameService startGameService,
                               GamePlayService gamePlayService, EndGameService endGameService,
                               GameSessionService gameSessionService) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.startGameService = startGameService;
        this.gamePlayService = gamePlayService;
        this.endGameService = endGameService;
        this.gameSessionService = gameSessionService;
    }

    /* 요청을 Sorted Set에 저장*/
    public void enqueueRequest(String gameRoomId, String requestDetails) {
        double score = System.currentTimeMillis(); // 요청 시간을 스코어로 사용
        String key = "gameRequests:" + gameRoomId;
        redisTemplate.opsForZSet().add(key, requestDetails, score);
    }

    public void processRequests(String gameRoomId) throws JsonProcessingException {
        String key = "gameRequests:" + gameRoomId;

        log.info("processRequests 시작");
        while (true) {
            Set<String> requests = redisTemplate.opsForZSet().range(key, 0, 0);
            if (requests != null && !requests.isEmpty()) {
                String requestJson = requests.iterator().next();
                GameRequest request = objectMapper.readValue(requestJson, GameRequest.class);

                log.info("executeGameRequest 시작");
                executeGameRequest(request);
                log.info("executeGameRequest 종료");

                redisTemplate.opsForZSet().remove(key, request);
                log.info("완료된 요청 삭제");
            } else break;
        }
    }

    private void executeGameRequest(GameRequest request) {
        log.info("요청 우선 순위대로 처리");

        Long gameRoomId = request.getGameRoomId();
        log.info("gameRoomId {}", gameRoomId);

        String gameState = request.getGameState();
        log.info("gameState -> {}", gameState);

        switch (gameState) {
            case "START" -> {
                GameDto.StartRoundResponse response = startGameService.startRound(gameRoomId, request.getEmail());
                sendUserGameMessage(response, request.getEmail());
            }
            case "ACTION", "USER_CHOICE" -> {
                Object response = switch (gameState) {
                    case "ACTION" ->
                            gamePlayService.playerAction(gameRoomId, request.getGameBetting());
                    case "USER_CHOICE" -> gameSessionService.processUserChoices(gameRoomId, request.getUserChoices());
                    default -> throw new IllegalStateException("Unexpected value: " + gameState);
                };
                String destination = "/topic/gameRoom/" + gameRoomId;
                messagingTemplate.convertAndSend(destination, response);
            }
            case "END" -> {
                GameDto.EndRoundResponse response = endGameService.endRound(gameRoomId, request.getEmail());
                sendUserEndRoundMessage(response, request.getEmail());
            }
            case "GAME_END" -> {
                GameDto.EndGameResponse response = endGameService.endGame(gameRoomId);
                sendUserEndGameMessage(response, request.getEmail());
            }

            default -> throw new IllegalStateException("Invalid game state: " + gameState);
        }
    }

    private void sendUserEndRoundMessage(GameDto.EndRoundResponse response, String email) {

        log.info("who are you? -> {}", email);
        log.info("player's Card : {}", response.getMyCard());

        try {
            messagingTemplate.convertAndSendToUser(email, "/queue/endRoundInfo", new EndRoundInfo(
                    response.getNowState(),
                    response.getNextState(),
                    response.getRound(),
                    response.getRoundWinner().getNickname(),
                    response.getRoundLoser().getNickname(),
                    response.getRoundPot(),
                    response.getMyCard()));
            log.info("Message sent successfully.");
        }
        catch (Exception e) {
            log.error("Failed to send message", e);
        }

    }

    private void sendUserEndGameMessage(GameDto.EndGameResponse response, String email) {

        log.info("who are you? -> {}", email);

        try {
            messagingTemplate.convertAndSendToUser(email, "/queue/endGameInfo", new EndGameInfo(
                    response.getNowState(),
                    response.getNextState(),
                    response.getGameWinner().getNickname(),
                    response.getGameLoser().getNickname(),
                    response.getWinnerPot(),
                    response.getLoserPot()));
            log.info("Message sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }

    private void sendUserGameMessage(GameDto.StartRoundResponse response, String email) {
        /* 각 Player 에게 상대 카드 정보와 턴 정보를 전송*/
        log.info("who are you? -> {}", email);
        log.info(response.getGameState(), response.getTurn().toString());
        String playerOne = response.getPlayerOne().getEmail();
        String playerTwo = response.getPlayerTwo().getEmail();
        try {
            if (email.equals(playerOne)) {
                messagingTemplate.convertAndSendToUser(playerOne, "/queue/gameInfo", new GameInfo(
                        response.getOtherCard(),
                        response.getTurn(),
                        response.getFirstBet(),
                        response.getRoundPot(),
                        response.getRound()));
                log.info("Message sent successfully.");
            }

            if (email.equals(playerTwo)) {
                messagingTemplate.convertAndSendToUser(playerTwo, "/queue/gameInfo", new GameInfo(
                        response.getOtherCard(),
                        response.getTurn(),
                        response.getFirstBet(),
                        response.getRoundPot(),
                        response.getRound()));
                log.info("Message sent successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }
}
