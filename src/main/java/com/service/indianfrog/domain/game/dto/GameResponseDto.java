package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.user.entity.User;
import lombok.Builder;

public class GameResponseDto {

    public record GameStatus(
            Long gameRoomId,
            String nickname,
            GameState gameState
    ) {}

    public record StartRoundResponse(
            String gameState,
            int round,
            User playerOne,
            User playerTwo,
            Card otherCard,
            Turn turn,
            int firstBet,
            int roundPot,
            int myPoint,
            int otherPoint
    ) {}

    public record EndRoundResponse(
            String nowState,
            String nextState,
            int round,
            User winner,
            User loser,
            int roundPot,
            Card myCard,
            Card otherCard,
            int winnerPoint,
            int loserPoint
    ) {}

    public record EndGameResponse(
            String nowState,
            String nextState,
            User gameWinner,
            User gameLoser,
            int winnerPot,
            int loserPot
    ) {}

    @Builder
    public record GameResult(
            User winner,
            User loser,
            int winnerPot,
            int loserPot
    ){}
}