package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameResponseDto.*;
import com.service.indianfrog.domain.game.dto.SendUserDto.EndGameInfo;
import com.service.indianfrog.domain.game.dto.SendUserDto.EndRoundInfo;
import com.service.indianfrog.domain.game.dto.SendUserDto.GameInfo;
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

    public void sendUserEndRoundMessage(EndRoundResponse response, Principal principal) {

        log.info("who are you? -> {}", principal.getName());
        log.info("player's Card : {}", response.myCard());

        try {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/endRoundInfo", new EndRoundInfo(
                    response.nowState(),
                    response.nextState(),
                    response.round(),
                    response.winner().getNickname(),
                    response.loser().getNickname(),
                    response.roundPot(),
                    response.myCard(),
                    response.otherCard(),
                    response.winnerPoint(),
                    response.loserPoint()));
            log.info("Message sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }

    }

    public void sendUserEndGameMessage(EndGameResponse response, Principal principal) {

        log.info("who are you? -> {}", principal.getName());

        try {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/endGameInfo", new EndGameInfo(
                    response.nowState(),
                    response.nextState(),
                    response.gameWinner().getNickname(),
                    response.gameLoser().getNickname(),
                    response.winnerPot(),
                    response.loserPot()));
            log.info("Message sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }

    public void sendUserGameMessage(StartRoundResponse response, Principal principal) {
        /* 각 Player 에게 상대 카드 정보와 턴 정보를 전송*/
        log.info("who are you? -> {}", principal.getName());
        log.info(response.gameState(), response.turn().toString());
        try {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/gameInfo", new GameInfo(
                    response.otherCard(),
                    response.turn(),
                    response.firstBet(),
                    response.roundPot(),
                    response.round(),
                    response.myPoint(),
                    response.otherPoint()));
            log.info("Message sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }
}
