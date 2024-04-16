package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameDto.EndGameResponse;
import com.service.indianfrog.domain.game.dto.GameDto.EndRoundResponse;
import com.service.indianfrog.domain.game.dto.GameResult;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.game.utils.RepositoryHolder;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Tag(name = "게임/라운드 종료 서비스", description = "게임/라운드 종료 서비스 로직")
@Slf4j
@Service
public class EndGameService {

    private final GameValidator gameValidator;
    private final RepositoryHolder repositoryHolder;
    private final GameTurnService gameTurnService;
    private final GameRoomRepository gameRoomRepository;

    public EndGameService(GameValidator gameValidator, RepositoryHolder repositoryHolder,
                          GameTurnService gameTurnService, GameRoomRepository gameRoomRepository) {
        this.gameValidator = gameValidator;
        this.repositoryHolder = repositoryHolder;
        this.gameTurnService = gameTurnService;
        this.gameRoomRepository = gameRoomRepository;
    }

    /* 라운드 종료 로직*/
    @Transactional
    public EndRoundResponse endRound(Long gameRoomId) {
        log.info("Ending round for gameRoomId={}", gameRoomId);
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);

        /* 라운드 승자 패자 결정
        승자에게 라운드 포인트 할당
        라운드 포인트 값 가져오기*/
        GameResult gameResult = determineGameResult(game);
        log.debug("Round result determined: winnerId={}, loserId={}", gameResult.getWinnerId(), gameResult.getLoserId());
        assignRoundPointsToWinner(game, gameResult);
        int roundPot = game.getPot();

        /* 라운드 승자가 선턴을 가지도록 설정*/
        initializeTurnForGame(game, gameResult.getWinnerId());

        /* 라운드 정보 초기화*/
        game.resetRound();
        log.debug("Round reset for gameRoomId={}", gameRoomId);

        /* 게임 상태 결정 : 다음 라운드 시작 상태 반환 or 게임 종료 상태 반환*/
        String gameState = determineGameState(game);
        log.info("Round ended for gameRoomId={}, newState={}", gameRoomId, gameState);

        return new EndRoundResponse(gameState, game.getRound(), gameResult.getWinnerId(), gameResult.getLoserId(), roundPot);
    }

    /* 게임 종료 로직*/
    @Transactional
    public EndGameResponse endGame(Long gameRoomId) {
        log.info("Ending game for gameRoomId={}", gameRoomId);
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);

        /* 게임 결과 처리 및 게임 정보 초기화*/
        GameResult gameResult = processGameResults(game);

        GameRoom CurrentGameStatus = gameRoomRepository.findByRoomId(gameRoomId);
        CurrentGameStatus.updateGameState(GameState.READY);

        log.info("Game ended for gameRoomId={}, winnerId={}, loserId={}",
                gameRoomId, gameResult.getWinnerId(), gameResult.getLoserId());

        /* 유저 선택 상태 반환*/
        return new EndGameResponse("USER_CHOICE", gameResult.getWinnerId(), gameResult.getLoserId(),
                gameResult.getWinnerPot(), gameResult.getLoserPot());
    }


    /* 검증 메서드 필드*/
    /* 라운드 승자, 패자 선정 메서드 */
    private GameResult determineGameResult(Game game) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        Card playerOneCard = game.getPlayerOneCard();
        Card playerTwoCard = game.getPlayerTwoCard();

        /* 카드 숫자가 같으면 1번 덱의 카드를 가진 플레이어가 승리*/
        GameResult result;
        if (playerOneCard.getNumber() != playerTwoCard.getNumber()) {
            result = playerOneCard.getNumber() > playerTwoCard.getNumber() ?
                    new GameResult(playerOne, playerTwo) : new GameResult(playerTwo, playerOne);
        } else {
            result = playerOneCard.getDeckNumber() == 1 ?
                    new GameResult(playerOne, playerTwo) : new GameResult(playerTwo, playerOne);
        }

        log.debug("Game result determined: winnerId={}, loserId={}", result.getWinnerId(), result.getLoserId());
        return result;
    }

    /* 라운드 포인트 승자에게 할당하는 메서드*/
    private void assignRoundPointsToWinner(Game game, GameResult gameResult) {
        User winner = repositoryHolder.userRepository.findById(gameResult.getWinnerId())
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        int pointsToAdd = game.getPot();

        winner.setPoints(winner.getPoints() + pointsToAdd);

        if (winner.equals(game.getPlayerOne())) {
            game.addPlayerOneRoundPoints(pointsToAdd);
        } else {
            game.addPlayerTwoRoundPoints(pointsToAdd);
        }
        log.info("Points assigned: winnerId={}, pointsAdded={}", winner.getId(), pointsToAdd);
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

        if (gameLoser.getPoints() < 10) {
            doRevivalGame(gameLoser);
        }

        /* 게임 데이터 초기화*/
        game.resetGame();

        return new GameResult(gameWinner, gameLoser, winnerTotalPoints, loserTotalPoints);
    }

    private void doRevivalGame(User gameLoser) {
        /*카드가 3장이 있을때, 세장 중 한장을 고르면 그 값만큼 포인트를 올려줌.*/
        Random random = new Random();
        int[] pointsIncreaseOptions = {75, 100, 125};
        int index = random.nextInt(pointsIncreaseOptions.length);
        gameLoser.increasePoints(pointsIncreaseOptions[index]);
    }

    /* 1라운드 이후 턴 설정 메서드*/
    private void initializeTurnForGame(Game game, Long winnerId) {
        List<User> players = new ArrayList<>();

        /* 전 라운드 승자를 해당 첫 턴으로 설정*/
        User roundWinner = repositoryHolder.userRepository.findById(winnerId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        players.add(roundWinner);

        User player = (!roundWinner.equals(game.getPlayerOne()))
                ? game.getPlayerTwo() : game.getPlayerOne();
        players.add(player);

        gameTurnService.setTurn(game.getId(), new Turn(players));
    }
}
