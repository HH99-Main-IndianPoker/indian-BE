package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.entity.Betting;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
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
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);
        User user = gameValidator.findUserByNickname(nickname);
        Turn turn = gameTurnService.getTurn(game.getId());
        if (turn == null || turn.getCurrentPlayer() == null) {
            throw new RestApiException(ErrorCode.GAME_USER_HAS_GONE.getMessage());
        }

        Betting betting = Betting.valueOf(action.toUpperCase());
        return switch (betting) {
            case CHECK -> performCheckAction(game, user, turn);
            case RAISE -> performRaiseAction(game, user);
            case DIE -> performDieAction(game, user);
        };
    }

    private GameState performCheckAction(Game game, User user, Turn turn) {
        /* 유저 턴 확인*/
        boolean isFirstTurn = turn.getCurrentPlayer().equals(user);

        if (!isFirstTurn) {
            int userPoints = user.getPoints();
            int currentBet = game.getBetAmount();
            if (userPoints >= currentBet) {
                user.setPoints(userPoints - currentBet);
                game.setPot(game.getPot() + currentBet);
            } else {
                game.setPot(game.getPot() + userPoints);
                user.setPoints(0);
            }
            return GameState.END;
        }

        /* 선턴 유저 CHECK*/
        user.setPoints(user.getPoints() - game.getBetAmount());
        game.setPot(game.getPot() + game.getBetAmount());
        return GameState.ACTION;
    }

    private GameState performRaiseAction(Game game, User user) {
        int userPoints = user.getPoints();

        if (userPoints <= 0) {
            return GameState.END;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int raiseAmount=0;

        /* RAISE 베팅 액 설정*/
        try {
            System.out.print("베팅할 금액을 입력하세요: ");
            String input = br.readLine();
            raiseAmount = Integer.parseInt(input);
        } catch (IOException e) {
            log.error("입력 오류가 발생했습니다.");
            raiseAmount = Math.min(raiseAmount, userPoints);
            e.printStackTrace();
        }


        user.setPoints(userPoints - raiseAmount);
        game.setPot(game.getPot() + raiseAmount);
        game.setBetAmount(raiseAmount);

        return GameState.ACTION;
    }

    private GameState performDieAction(Game game, User user) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();
        User winner = user.equals(playerOne) ? playerTwo : playerOne;

        /* DIE 하지 않은 유저에게 Pot 이월*/
        int pot = game.getPot();
        if (winner.equals(playerOne)) {
            game.addPlayerOneRoundPoints(pot);
        } else {
            game.addPlayerTwoRoundPoints(pot);
        }

        game.setFoldedUser(user);

        return GameState.END;
    }

}
