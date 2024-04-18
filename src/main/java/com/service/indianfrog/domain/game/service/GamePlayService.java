package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.ActionDto;
import com.service.indianfrog.domain.game.dto.GameBetting;
import com.service.indianfrog.domain.game.entity.Betting;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.repository.GameRepository;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Tag(name = "게임 플레이 서비스", description = "게임 플레이 서비스 로직")
@Slf4j
@Service
public class GamePlayService {

    private final GameValidator gameValidator;
    private final GameTurnService gameTurnService;
    private final GameRepository gameRepository;

    public GamePlayService(GameValidator gameValidator, GameTurnService gameTurnService, GameRepository gameRepository) {
        this.gameValidator = gameValidator;
        this.gameTurnService = gameTurnService;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public ActionDto playerAction(Long gameRoomId, GameBetting gameBetting, String action) {
        log.info("Action received: gameRoomId={}, nickname={}, action={}", gameRoomId, gameBetting.getNickname(), action);
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);
        User user = gameValidator.findUserByNickname(gameBetting.getNickname());
        Turn turn = gameTurnService.getTurn(game.getId());

        /* 유저의 턴이 맞는지 확인*/
        if (!turn.getCurrentPlayer().equals(user.getNickname())) {
            log.warn("It's not the turn of the user: {}", gameBetting.getNickname());
            throw new IllegalStateException("당신의 턴이 아닙니다, 선턴 유저의 행동이 끝날 때까지 기다려 주세요.");
        }

        log.info("Performing {} action for user {}", action, gameBetting.getNickname());
        Betting betting = Betting.valueOf(action.toUpperCase());
        return switch (betting) {
            case CHECK -> performCheckAction(game, user, turn);
            case RAISE -> performRaiseAction(game, user, turn, gameBetting.getPoint());
            case DIE -> performDieAction(game, user);
        };

    }

    private ActionDto performCheckAction(Game game, User user, Turn turn) {
        /* 유저 턴 확인*/
        log.info("Check action: currentPlayer={}, user={}, currentPot={}, betAmount={}",
                user.getNickname(), user.getEmail(), game.getPot(), game.getBetAmount());

        if (game.isCheckStatus()) {
            return gameEnd(user, game);
        }

        if(game.isRaiseStatus()){
            return gameEnd(user, game);
        }

        /* 선턴 유저 CHECK*/
        user.setPoints(user.getPoints() - game.getBetAmount());
        game.setPot(game.getPot() + game.getBetAmount());
        game.updateCheck();
        turn.nextTurn();
        log.info("First turn check completed, moving to next turn");

        gameRepository.save(game);

        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.ACTION)
                .actionType(Betting.CHECK)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(turn.getCurrentPlayer())
                .build();
    }

    private ActionDto performRaiseAction(Game game, User user, Turn turn, int raiseAmount) {
        int userPoints = user.getPoints();
        log.info("Raise action initiated by user: {}, currentPoints={}", user.getEmail(), userPoints);

        if (userPoints <= 0) {
            log.info("User has insufficient points to raise");
            return new ActionDto(GameState.ACTION, GameState.END, Betting.RAISE, 0, game.getPot(), user.getNickname());
        }
        /* RAISE 베팅 액 설정*/
        log.info("Raise amount entered: {}", raiseAmount);

        user.setPoints(userPoints - (game.getBetAmount() + raiseAmount));
        game.setPot(game.getPot() + (game.getBetAmount() + raiseAmount));
        game.setBetAmount(raiseAmount);
        game.updateRaise();
        turn.nextTurn();
        log.info("Raise action completed: newPot={}, newBetAmount={}", game.getPot(), game.getBetAmount());

        gameRepository.save(game);

        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.ACTION)
                .actionType(Betting.CHECK)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(turn.getCurrentPlayer())
                .build();
    }

    private ActionDto performDieAction(Game game, User user) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();
        User winner = user.equals(playerOne) ? playerTwo : playerOne;
        log.info("Die action by user: {}, winner: {}", user.getNickname(), winner.getNickname());

        /* DIE 하지 않은 유저에게 Pot 이월*/
        int pot = game.getPot();
        if (winner.equals(playerOne)) {
            game.addPlayerOneRoundPoints(pot);
        } else {
            game.addPlayerTwoRoundPoints(pot);
        }

        game.setFoldedUser(user);

        log.info("Die action completed, game ended. Winner: {}", winner.getNickname());

        gameRepository.save(game);

        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.END)
                .actionType(Betting.DIE)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(winner.getNickname())
                .build();
    }

    private ActionDto gameEnd(User user, Game game) {
        log.info("User points before action: {}, currentBet={}", user.getPoints(), game.getBetAmount());
        if (user.getPoints() >= game.getBetAmount()) {
            user.setPoints(user.getPoints() - game.getBetAmount());
            game.setPot(game.getPot() + game.getBetAmount());
        } else {
            game.setPot(game.getPot() + user.getPoints());
            user.setPoints(0);
        }
        log.info("Check completed, game state updated: newPot={}, newUserPoints={}", game.getPot(), user.getPoints());

        gameRepository.save(game);

        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.END)
                .actionType(Betting.CHECK)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(user.getNickname())
                .build();
    }

}
