package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Turn;

public class SendUserDto {

    public record EndGameInfo(
            String nowState,
            String nextState,
            String gameWinner,
            String gameLoser,
            int winnerPot,
            int loserPot
    ) {}

    public record EndRoundInfo(
            String nowState,
            String nextState,
            int round,
            String roundWinner,
            String roundLoser,
            int roundPot,
            Card myCard,
            Card otherCard,
            int winnerPoint,
            int loserPoint
    ) {}

    public record GameInfo(
            Card otherCard,
            Turn turn,
            int firstBet,
            int roundPot,
            int round,
            int myPoint,
            int otherPoint
    ) {}

}
