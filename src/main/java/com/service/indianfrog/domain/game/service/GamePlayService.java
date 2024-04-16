package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.entity.Betting;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
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

    public GamePlayService(GameValidator gameValidator, GameTurnService gameTurnService) {
        this.gameValidator = gameValidator;
        this.gameTurnService = gameTurnService;
    }

    @Transactional
    public GameState playerAction(Long gameRoomId, String nickname, String action) {
        log.info("Action received: gameRoomId={}, nickname={}, action={}", gameRoomId, nickname, action);
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);
        User user = gameValidator.findUserByNickname(nickname);
        Turn turn = gameTurnService.getTurn(game.getId());

        /* 유저의 턴이 맞는지 확인*/
        if (!turn.getCurrentPlayer().equals(user.getNickname())) {
            log.warn("It's not the turn of the user: {}", nickname);
            throw new IllegalStateException("당신의 턴이 아닙니다, 선턴 유저의 행동이 끝날 때까지 기다려 주세요.");
        }

        log.debug("Performing {} action for user {}", action, nickname);
        Betting betting = Betting.valueOf(action.toUpperCase());
        return switch (betting) {
            case CHECK -> performCheckAction(game, user, turn);
            case RAISE -> performRaiseAction(game, user, turn);
            case DIE -> performDieAction(game, user);
        };
    }

    private GameState performCheckAction(Game game, User user, Turn turn) {
        /* 유저 턴 확인*/
        boolean isFirstTurn = turn.getCurrentPlayer().equals(user.getNickname());
        log.debug("Check action: isFirstTurn={}, user={}, currentPot={}, betAmount={}",
                isFirstTurn, user.getEmail(), game.getPot(), game.getBetAmount());

        if (game.isCheck()) {
            return gameEnd(user, game);
        }

        if(game.isRaise()){
            return gameEnd(user, game);
        }

        /* 선턴 유저 CHECK*/
        user.setPoints(user.getPoints() - game.getBetAmount());
        game.setPot(game.getPot() + game.getBetAmount());
        game.updateCheck();
        turn.nextTurn();
        log.info("First turn check completed, moving to next turn");
        return GameState.ACTION;
    }

    private GameState gameEnd(User user, Game game) {
        log.debug("User points before action: {}, currentBet={}", user.getPoints(), game.getBetAmount());
        if (user.getPoints() >= game.getBetAmount()) {
            user.setPoints(user.getPoints() - game.getBetAmount());
            game.setPot(game.getPot() + game.getBetAmount());
        } else {
            game.setPot(game.getPot() + user.getPoints());
            user.setPoints(0);
        }
        log.info("Check completed, game state updated: newPot={}, newUserPoints={}", game.getPot(), user.getPoints());
        return GameState.END;
    }

    private GameState performRaiseAction(Game game, User user, Turn turn) {
        int userPoints = user.getPoints();
        log.debug("Raise action initiated by user: {}, currentPoints={}", user.getEmail(), userPoints);

        if (userPoints <= 0) {
            log.warn("User has insufficient points to raise");
            return GameState.END;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int raiseAmount = 0;

        /* RAISE 베팅 액 설정*/
        try {
            System.out.print("베팅할 금액을 입력하세요: ");
            String input = br.readLine();
            raiseAmount = Integer.parseInt(input);
            log.debug("Raise amount entered: {}", raiseAmount);
        } catch (IOException e) {
            log.error("입력 오류가 발생했습니다.");
            raiseAmount = Math.min(raiseAmount, userPoints);
            e.printStackTrace();
        }

        user.setPoints(userPoints - raiseAmount);
        game.setPot(game.getPot() + raiseAmount);
        game.setBetAmount(raiseAmount);
        game.updateRaise();
        turn.nextTurn();
        log.info("Raise action completed: newPot={}, newBetAmount={}", game.getPot(), game.getBetAmount());
        return GameState.ACTION;
    }

    private GameState performDieAction(Game game, User user) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();
        User winner = user.equals(playerOne) ? playerTwo : playerOne;
        log.info("Die action by user: {}, winner: {}", user.getEmail(), winner.getEmail());

        /* DIE 하지 않은 유저에게 Pot 이월*/
        int pot = game.getPot();
        if (winner.equals(playerOne)) {
            game.addPlayerOneRoundPoints(pot);
        } else {
            game.addPlayerTwoRoundPoints(pot);
        }

        game.setFoldedUser(user);
        log.info("Die action completed, game ended. Winner: {}", winner.getEmail());
        return GameState.END;
    }

}
