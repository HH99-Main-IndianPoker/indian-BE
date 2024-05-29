package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.EndGameInfo;
import com.service.indianfrog.domain.game.dto.EndRoundInfo;
import com.service.indianfrog.domain.game.dto.GameDto;
import com.service.indianfrog.domain.game.dto.GameInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@Slf4j
public class SendUserMessageService {

    private final SimpMessageSendingOperations messagingTemplate;

    public SendUserMessageService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendUserEndRoundMessage(GameDto.EndRoundResponse response, Principal principal) {

        log.info("who are you? -> {}", principal.getName());
        log.info("player's Card : {}", response.getMyCard());

        try {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/endRoundInfo", new EndRoundInfo(
                    response.getNowState(),
                    response.getNextState(),
                    response.getRound(),
                    response.getRoundWinner().getNickname(),
                    response.getRoundLoser().getNickname(),
                    response.getRoundPot(),
                    response.getMyCard(),
                    response.getOtherCard(),
                    response.getWinnerPoint(),
                    response.getLoserPoint()));
            log.info("Message sent successfully.");
        }
        catch (Exception e) {
            log.error("Failed to send message", e);
        }

    }

    public void sendUserEndGameMessage(GameDto.EndGameResponse response, Principal principal) {

        log.info("who are you? -> {}", principal.getName());

        try {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/endGameInfo", new EndGameInfo(
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

    public void sendUserGameMessage(GameDto.StartRoundResponse response, Principal principal) {
        /* 각 Player 에게 상대 카드 정보와 턴 정보를 전송*/
        log.info("who are you? -> {}", principal.getName());
        log.info(response.getGameState(), response.getTurn().toString());
        String playerOne = response.getPlayerOne().getEmail();
        String playerTwo = response.getPlayerTwo().getEmail();
        try {
            if (principal.getName().equals(playerOne)) {
                messagingTemplate.convertAndSendToUser(playerOne, "/queue/gameInfo", new GameInfo(
                        response.getOtherCard(),
                        response.getTurn(),
                        response.getFirstBet(),
                        response.getRoundPot(),
                        response.getRound(),
                        response.getMyPoint(),
                        response.getOtherPoint()));
                log.info("Message sent successfully.");
            }

            if (principal.getName().equals(playerTwo)) {
                messagingTemplate.convertAndSendToUser(playerTwo, "/queue/gameInfo", new GameInfo(
                        response.getOtherCard(),
                        response.getTurn(),
                        response.getFirstBet(),
                        response.getRoundPot(),
                        response.getRound(),
                        response.getMyPoint(),
                        response.getOtherPoint()));
                log.info("Message sent successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }
}
