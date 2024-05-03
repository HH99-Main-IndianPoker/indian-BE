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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Tag(name = "게임 플레이 서비스", description = "게임 플레이 서비스 로직")
@Slf4j
@Service
public class GamePlayService {

    private final GameValidator gameValidator;
    private final GameTurnService gameTurnService;
    private final GameRepository gameRepository;
    private final MeterRegistry registry;
    private final Timer totalGamePlayTimer;
    @PersistenceContext
    private EntityManager em;

    public GamePlayService(GameValidator gameValidator, GameTurnService gameTurnService,
                           GameRepository gameRepository, MeterRegistry registry) {
        this.gameValidator = gameValidator;
        this.gameTurnService = gameTurnService;
        this.gameRepository = gameRepository;
        this.registry = registry;
        this.totalGamePlayTimer = registry.timer("totalGamePlay.time");
    }

    @Transactional
    public ActionDto playerAction(Long gameRoomId, GameBetting gameBetting, String action) {
        return totalGamePlayTimer.record(() -> {
            log.info("Action received: gameRoomId={}, nickname={}, action={}", gameRoomId, gameBetting.getNickname(), action);
            GameRoom gameRoom = em.find(GameRoom.class, gameRoomId, LockModeType.PESSIMISTIC_WRITE);
            Game game = gameRoom.getCurrentGame();
            User user = gameValidator.findUserByNickname(gameBetting.getNickname());
            Turn turn = gameTurnService.getTurn(game.getId());
            User otherUser = null;

            if (user.equals(game.getPlayerTwo())) {
                otherUser = game.getPlayerOne();
            } else if (user.equals(game.getPlayerOne())){
                otherUser = game.getPlayerTwo();
            }

            /* 유저의 턴이 맞는지 확인*/
            if (!turn.getCurrentPlayer().equals(user.getNickname())) {
                log.warn("It's not the turn of the user: {}", gameBetting.getNickname());
                throw new IllegalStateException("당신의 턴이 아닙니다, 선턴 유저의 행동이 끝날 때까지 기다려 주세요.");
            }

            log.info("Performing {} action for user {}", action, gameBetting.getNickname());
            Betting betting = Betting.valueOf(action.toUpperCase());
            return switch (betting) {
                case CHECK -> performCheckAction(game, user, turn, otherUser);
                case RAISE -> performRaiseAction(game, user, turn, gameBetting.getPoint(), otherUser);
                case DIE -> performDieAction(game, user, otherUser);
            };
        });
    }

    @Transactional
    public ActionDto performCheckAction(Game game, User user, Turn turn, User otherUser) {
        /* 유저 턴 확인*/
        Timer.Sample checkTimer = Timer.start(registry);
        log.info("Check action: currentPlayer={}, user={}, currentPot={}, betAmount={}",
                user.getNickname(), user.getEmail(), game.getPot(), game.getBetAmount());

        if (game.isCheckStatus()) {
            return gameEnd(user, game, otherUser);
        }

        if(game.isRaiseStatus()){
            return gameEnd(user, game, otherUser);
        }

        /* 유저 CHECK */
        user.decreasePoints(game.getBetAmount());
        game.updatePot(game.getBetAmount());
        game.updateCheck();
        turn.nextTurn();
        log.info("First turn check completed, moving to next turn " + turn.getCurrentPlayer());

        checkTimer.stop(registry.timer("playCheck.time"));
        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.ACTION)
                .actionType(Betting.CHECK)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(turn.getCurrentPlayer())
                .previousPlayer(user.getNickname())
                .myPoint(user.getPoints())
                .otherPoint(otherUser.getPoints())
                .build();
    }

    @Transactional
    public ActionDto performRaiseAction(Game game, User user, Turn turn, int raiseAmount, User otherUser) {
        Timer.Sample raiseTimer = Timer.start(registry);
        int userPoints = user.getPoints();
        log.info("Raise action initiated by user: {}, currentPoints={}", user.getNickname(), userPoints);

        if (userPoints <= 0) {
            log.info("User has insufficient points to raise");
            return ActionDto.builder()
                    .nowState(GameState.ACTION)
                    .nextState(GameState.END)
                    .actionType(Betting.RAISE)
                    .nowBet(0)
                    .pot(game.getPot())
                    .currentPlayer(user.getNickname())
                    .previousPlayer(user.getNickname())
                    .myPoint(user.getPoints())
                    .otherPoint(otherUser.getPoints())
                    .build();
        }

        if (userPoints - game.getBetAmount() <= 0 || userPoints < game.getBetAmount() + raiseAmount){
            performCheckAction(game, user, turn, otherUser);
        }

        /* RAISE 베팅 액 설정*/
        log.info("Raise amount entered: {}", raiseAmount);
        user.decreasePoints(game.getBetAmount() + raiseAmount);
        game.updatePot(game.getBetAmount() + raiseAmount);
        game.setBetAmount(raiseAmount);
        game.updateRaise();
        turn.nextTurn();
        log.info("Raise action completed: newPot={}, newBetAmount={}, afterRaisePoint={}", game.getPot(), game.getBetAmount(), user.getPoints());

        raiseTimer.stop(registry.timer("playRaise.time"));
        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.ACTION)
                .actionType(Betting.RAISE)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(turn.getCurrentPlayer())
                .previousPlayer(user.getNickname())
                .myPoint(user.getPoints())
                .otherPoint(otherUser.getPoints())
                .build();
    }

    @Transactional
    public ActionDto performDieAction(Game game, User user, User otherUser) {
        Timer.Sample dieTimer = Timer.start(registry);
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
        game.setBetAmount(0);

        log.info("Die action completed, game ended. Winner: {}", winner.getNickname());

        dieTimer.stop(registry.timer("playDie.time"));
        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.END)
                .actionType(Betting.DIE)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(winner.getNickname())
                .previousPlayer(game.getFoldedUser().getNickname())
                .myPoint(user.getPoints())
                .otherPoint(otherUser.getPoints())
                .build();
    }

    @Transactional
    public ActionDto gameEnd(User user, Game game, User otherUser) {
        log.info("User points before action: {}, currentBet={}", user.getPoints(), game.getBetAmount());

        int betPoint = user.getPoints() > 0 ? game.getBetAmount() : 0;
        user.decreasePoints(betPoint);
        game.updatePot(betPoint);

        log.info("Check completed, game state updated: newPot={}, newUserPoints={}", game.getPot(), user.getPoints());

        gameRepository.save(game);

        return ActionDto.builder()
                .nowState(GameState.ACTION)
                .nextState(GameState.END)
                .actionType(Betting.CHECK)
                .nowBet(game.getBetAmount())
                .pot(game.getPot())
                .currentPlayer(user.getNickname())
                .previousPlayer(user.getNickname())
                .myPoint(user.getPoints())
                .otherPoint(otherUser.getPoints())
                .build();
    }
}
