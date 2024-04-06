package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameDto.EndGameResponse;
import com.service.indianfrog.domain.game.dto.GameDto.EndRoundResponse;
import com.service.indianfrog.domain.game.dto.GameResult;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameRoom;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.game.utils.RepositoryHolder;
import com.service.indianfrog.domain.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Tag(name = "게임/라운드 종료 서비스", description = "게임/라운드 종료 서비스 로직")
@Slf4j
@Service
public class EndGameService {

    private final GameValidator gameValidator;

    public EndGameService(GameValidator gameValidator) {
        this.gameValidator = gameValidator;
    }

    /* 라운드 종료 로직*/
    @Transactional
    public EndRoundResponse endRound(Long gameRoomId) {
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);

        /* 라운드 승자 패자 결정
        승자에게 라운드 포인트 할당
        라운드 포인트 값 가져오기*/
        GameResult gameResult = determineGameResult(game);
        assignRoundPointsToWinner(game, gameResult);
        int roundPot = game.getPot();

        /* 라운드 정보 초기화*/
        game.resetRound();

        /* 게임 상태 결정 : 다음 라운드 시작 상태 반환 or 게임 종료 상태 반환*/
        String gameState = determineGameState(game);

        return new EndRoundResponse(gameState, game.getRound(), gameResult.getWinner(), gameResult.getLoser(), roundPot);
    }

    /* 게임 종료 로직*/
    @Transactional
    public EndGameResponse endGame(Long gameRoomId) {
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);

        /* 게임 결과 처리 및 게임 정보 초기화*/
        GameResult gameResult = processGameResults(game);

        /* 유저 선택 상태 반환*/
        return new EndGameResponse("USER_CHOICE", gameResult.getWinner(), gameResult.getLoser(),
                gameResult.getWinnerPot(), gameResult.getLoserPot());
    }


    /* 검증 메서드 필드*/
    /* 라운드 승자, 패자 선정 메서드 */
    private GameResult determineGameResult(Game game) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        Card playerOneCard = game.getPlayerOneCard();
        Card playerTwoCard = game.getPlayerTwoCard();

        if (playerOneCard.getNumber() != playerTwoCard.getNumber()) {
            return playerOneCard.getNumber() > playerTwoCard.getNumber() ?
                    new GameResult(playerOne, playerTwo) : new GameResult(playerTwo, playerOne);
        }

        /* 카드 숫자가 같으면 1번 덱의 카드를 가진 플레이어가 승리*/
        return playerOneCard.getDeckNumber() == 1 ?
                new GameResult(playerOne, playerTwo) : new GameResult(playerTwo, playerOne);
    }

    /* 라운드 포인트 승자에게 할당하는 메서드*/
    private void assignRoundPointsToWinner(Game game, GameResult gameResult) {
        User winner = gameResult.getWinner();
        if (winner != null) {
            int pointsToAdd = game.getPot();
            if (winner.equals(game.getPlayerOne())) {
                game.addPlayerOneRoundPoints(pointsToAdd);
            } else {
                game.addPlayerTwoRoundPoints(pointsToAdd);
            }
        }
    }

    /* 게임 내 라운드가 모두 종료되었는지 확인하는 메서드*/
    private String determineGameState(Game game) {
        /* 한 게임의 라운드는 현재 3라운드 까지임
        * 라운드 정보를 확인해 3 라운드일 경우 게임 종료 상태를 반환
        * 라운드 정보가 3보다 적은 경우 다음 라운드 시작을 위한 상태 반환
        * game.getRound >= 3 비교 과정을 게임 시작 시 유저의 입력 값을 통해
        * maxRound 필드 등을 만들어서 비교하는 등의 개선도 가능*/
        if (game.getRound() >= 3) {
            return "GAME_END";
        }
        return "START";
    }

    /* 게임 결과 처리 메서드*/
    private GameResult processGameResults(Game game) {
        int playerOneTotalPoints = game.getPlayerOneRoundPoints();
        int playerTwoTotalPoints = game.getPlayerTwoRoundPoints();

        /* 게임 승자와 패자를 정하고 각각의 정보 업데이트*/
        User gameWinner = playerOneTotalPoints > playerTwoTotalPoints ? game.getPlayerOne() : game.getPlayerTwo();
        User gameLoser = gameWinner.equals(game.getPlayerOne()) ? game.getPlayerTwo() : game.getPlayerOne();

        gameWinner.incrementWins();
        gameLoser.incrementLosses();

        /* 승자와 패자의 총 획득 포인트*/
        int winnerTotalPoints = gameWinner.equals(game.getPlayerOne()) ? playerOneTotalPoints : playerTwoTotalPoints;
        int loserTotalPoints = gameLoser.equals(game.getPlayerOne()) ? playerOneTotalPoints : playerTwoTotalPoints;

        /* 게임 데이터 초기화*/
        game.resetGame();

        return new GameResult(gameWinner, gameLoser, winnerTotalPoints, loserTotalPoints);
    }
}
