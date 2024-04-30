package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameDto.EndGameResponse;
import com.service.indianfrog.domain.game.dto.GameDto.EndRoundResponse;
import com.service.indianfrog.domain.game.dto.GameResult;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "게임/라운드 종료 서비스", description = "게임/라운드 종료 서비스 로직")
@Slf4j
@Service
public class EndGameService {

    private final GameTurnService gameTurnService;
    private final GameRoomRepository gameRoomRepository;
    private final ValidateRoomRepository validateRoomRepository;
    private final UserRepository userRepository;
    private final MeterRegistry registry;
    private final Timer totalRoundEndTimer;
    private final Timer totalGameEndTimer;
    private final GameValidator gameValidator;

    @PersistenceContext
    private EntityManager em;

    public EndGameService(GameTurnService gameTurnService, GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository, UserRepository userRepository,
                          MeterRegistry registry,GameValidator gameValidator) {
        this.gameTurnService = gameTurnService;
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
        this.userRepository = userRepository;
        this.registry = registry;
        this.totalRoundEndTimer = registry.timer("totalRoundEnd.time");
        this.totalGameEndTimer = registry.timer("totalGameEnd.time");
        this.gameValidator = gameValidator;
    }

    /* 라운드 종료 로직*/
    @Transactional
    public EndRoundResponse endRound(Long gameRoomId, String email) {
        return totalRoundEndTimer.record(() -> {
            log.info("Ending round for gameRoomId={}", gameRoomId);

//            GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
            GameRoom gameRoom = em.find(GameRoom.class, gameRoomId, LockModeType.PESSIMISTIC_WRITE);
            Game game = gameRoom.getCurrentGame();

            /* 라운드 승자 패자 결정
            승자에게 라운드 포인트 할당
            라운드 포인트 값 가져오기*/
            Timer.Sample gameResultTimer = Timer.start(registry);
            GameResult gameResult = determineGameResult(game);
            gameResultTimer.stop(registry.timer("roundResult.time"));

            Card myCard = null;

            if (email.equals(game.getPlayerOne().getEmail())) {
                myCard = game.getPlayerOneCard();
            }

            if(email.equals(game.getPlayerTwo().getEmail())) {
                myCard = game.getPlayerTwoCard();
            }

            log.info("myCard : {}", myCard);

            if (!game.isRoundEnded()) {
                Timer.Sample roundPointsTimer = Timer.start(registry);
                assignRoundPointsToWinner(game, gameResult);
                roundPointsTimer.stop(registry.timer("roundPoints.time"));

                /* 라운드 승자가 선턴을 가지도록 설정*/
                initializeTurnForGame(game, gameResult);
                game.updateRoundEnded();
            }

            log.info("Round result determined: winnerId={}, loserId={}", gameResult.getWinner().getNickname(), gameResult.getLoser().getNickname());

            int roundPot = game.getPot();

           if(game.isRoundEnded()) {
               /* 라운드 정보 초기화 */
               game.resetRound();
           }

            log.debug("Round reset for gameRoomId={}", gameRoomId);

            /* 게임 상태 결정 : 다음 라운드 시작 상태 반환 or 게임 종료 상태 반환*/
            String nextState = determineGameState(game);
            log.info("Round ended for gameRoomId={}, newState={}", gameRoomId, nextState);

            return new EndRoundResponse("END", nextState, game.getRound(), gameResult.getWinner(), gameResult.getLoser(), roundPot, myCard, gameResult.getWinner().getPoints(), gameResult.getLoser().getPoints());
        });
    }

    /* 게임 종료 로직*/
    @Transactional
    public EndGameResponse endGame(Long gameRoomId, String email) {
        return totalGameEndTimer.record(() -> {
            log.info("Ending game for gameRoomId={}", gameRoomId);
            GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);

            User user = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

            ValidateRoom validateRoom = validateRoomRepository.findByGameRoomAndParticipants(gameRoom, user.getNickname()).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_USER.getMessage()));
            validateRoom.resetReady();

            Game game = gameRoom.getCurrentGame();

            /* 게임 결과 처리 및 게임 정보 초기화*/
            Timer.Sample gameResultTimer = Timer.start(registry);
            GameResult gameResult = processGameResults(game, email);
            gameResultTimer.stop(registry.timer("endGameResult.time"));

            GameRoom CurrentGameStatus = gameRoomRepository.findByRoomId(gameRoomId);

            CurrentGameStatus.updateGameState(GameState.READY);

            log.info("Game ended for gameRoomId={}, winnerId={}, loserId={}, winnerPot={}, loserPot={}",
                    gameRoomId, gameResult.getWinner().getNickname(), gameResult.getLoser().getNickname(), gameResult.getWinnerPot(), gameResult.getLoserPot());

            /* 유저 선택 상태 반환 */
            return new EndGameResponse("GAME_END", "READY", gameResult.getWinner(), gameResult.getLoser(),
                    gameResult.getWinnerPot(), gameResult.getLoserPot());
        });
    }

    /* 검증 메서드 필드*/
    /* 라운드 승자, 패자 선정 메서드 */
    @Transactional
//    @Lock(LockModeType.PESSIMISTIC_READ)
    public GameResult determineGameResult(Game game) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        if (game.getFoldedUser() != null && game.getFoldedUser().equals(playerOne)) {
            return new GameResult(playerTwo, playerOne);
        } else if(game.getFoldedUser() != null && game.getFoldedUser().equals(playerTwo)){
            return new GameResult(playerOne, playerTwo);
        }

        GameResult result = getGameResult(game, playerOne, playerTwo);

        log.info("Game result determined: winnerId={}, loserId={}", result.getWinner(), result.getLoser());
        return result;
    }

    @Transactional
    public GameResult getGameResult(Game game, User playerOne, User playerTwo) {
        Card playerOneCard = game.getPlayerOneCard();
        Card playerTwoCard = game.getPlayerTwoCard();

        log.info("{} Card : {}", playerOne.getNickname(), game.getPlayerOneCard());
        log.info("{} Card : {}", playerTwo.getNickname(), game.getPlayerTwoCard());

        /* 카드 숫자가 같으면 1번 덱의 카드를 가진 플레이어가 승리*/
        GameResult result = null;

        if (playerOneCard.getNumber() != playerTwoCard.getNumber()) {
            result = playerOneCard.getNumber() > playerTwoCard.getNumber() ?
                    new GameResult(playerOne, playerTwo) : new GameResult(playerTwo, playerOne);
        }

        if (playerOneCard.getNumber() == playerTwoCard.getNumber()) {
            result = playerOneCard.getDeckNumber() == 1 ?
                    new GameResult(playerOne, playerTwo) : new GameResult(playerTwo, playerOne);
        }

        return result;
    }

    /* 라운드 포인트 승자에게 할당하는 메서드*/
    @Transactional
    public void assignRoundPointsToWinner(Game game, GameResult gameResult) {
        User winner = gameResult.getWinner();

        int pointsToAdd = game.getPot();

        winner.updatePoint(pointsToAdd);

        if (winner.equals(game.getPlayerOne())) {
            game.addPlayerOneRoundPoints(pointsToAdd);
        } else {
            game.addPlayerTwoRoundPoints(pointsToAdd);
        }

        log.info("Points assigned: winnerId={}, pointsAdded={}", winner.getNickname(), pointsToAdd);
    }

    /* 게임 내 라운드가 모두 종료되었는지 확인하는 메서드 */
    /* 수정 필요 - 유저 포인트가 0이 있을 때 하는 방법 */
    private String determineGameState(Game game) {
        /* 한 게임의 라운드는 현재 3라운드 까지임
         * 라운드 정보를 확인해 3 라운드일 경우 게임 종료 상태를 반환
         * 라운드 정보가 3보다 적은 경우 다음 라운드 시작을 위한 상태 반환
         * game.getRound >= 3 비교 과정을 게임 시작 시 유저의 입력 값을 통해
         * maxRound 필드 등을 만들어서 비교하는 등의 개선도 가능
         * 게임에 참가 중인 유저의 포인트를 확인해 0이 있을 경우 게임 종료 상태 반환*/
        /* 3라운드 종료 시*/
        if (game.getRound() >= 3) {
            return "GAME_END";
        }

        /* 플레이어의 포인트가 없을 때*/
        if (!checkPlayerPoints(game)) {
            return "GAME_END";
        }

        /* 정상 실행 상태 */
        return "START";
    }

    /* 게임 결과 처리 메서드*/
    @Transactional
    public GameResult processGameResults(Game game, String email) {
        int playerOneTotalPoints = game.getPlayerOneRoundPoints();
        int playerTwoTotalPoints = game.getPlayerTwoRoundPoints();

        log.info("playerOneTotalPoint : {}", playerOneTotalPoints);
        log.info("playerTwoTotalPoint : {}", playerTwoTotalPoints);

        /* 게임 승자와 패자를 정하고 각각의 정보 업데이트*/
        User gameWinner = playerOneTotalPoints > playerTwoTotalPoints ? game.getPlayerOne() : game.getPlayerTwo();
        User gameLoser = gameWinner.equals(game.getPlayerOne()) ? game.getPlayerTwo() : game.getPlayerOne();

        if (email.equals(gameWinner.getEmail())){
            gameWinner.incrementWins();
        }

        if (email.equals(gameLoser.getEmail())){
            gameLoser.incrementLosses();
        }

        /* 승자와 패자의 총 획득 포인트*/
        int winnerTotalPoints = gameWinner.equals(game.getPlayerOne()) ? playerOneTotalPoints : playerTwoTotalPoints;
        int loserTotalPoints = gameLoser.equals(game.getPlayerOne()) ? playerOneTotalPoints : playerTwoTotalPoints;

        log.info("winnerTotalPoints : {}", winnerTotalPoints);
        log.info("loserTotalPoints : {}", loserTotalPoints);

        /* 게임 데이터 초기화*/
        game.resetGame();

        log.info("winnerTotalPoints : {}", winnerTotalPoints);
        log.info("loserTotalPoints : {}", loserTotalPoints);

        return new GameResult(gameWinner, gameLoser, winnerTotalPoints, loserTotalPoints);
    }

    /* 1라운드 이후 턴 설정 메서드 */
    @Transactional
    public void initializeTurnForGame(Game game, GameResult gameResult) {
        List<User> players = new ArrayList<>();

        /* 전 라운드 승자를 해당 첫 턴으로 설정*/
        players.add(gameResult.getWinner());
        players.add(gameResult.getLoser());

        Turn turn = new Turn(players);
        gameTurnService.setTurn(game.getId(), turn);
    }

    private boolean checkPlayerPoints(Game game) {
        return game.getPlayerOne().getPoints() > 0 && game.getPlayerTwo().getPoints() > 0;
    }
}