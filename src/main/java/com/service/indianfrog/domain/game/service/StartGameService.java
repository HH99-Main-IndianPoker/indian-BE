package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameResponseDto.*;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
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

import java.util.*;

@Tag(name = "라운드 시작 서비스", description = "게임(라운드) 시작 서비스 로직")
@Slf4j(topic = "게임 시작 서비스 레이어")
@Service
public class StartGameService {

    /* 생성자를 통한 필드 주입 */
    private final GameTurnService gameTurnService;
    private final Timer totalRoundStartTimer;
    private final Timer performRoundStartTimer;
    @PersistenceContext
    private EntityManager em;

    public StartGameService(GameTurnService gameTurnService, MeterRegistry registry) {
        this.gameTurnService = gameTurnService;
        this.totalRoundStartTimer = registry.timer("totalRoundStart.time");
        this.performRoundStartTimer = registry.timer("performRoundStart.time");
    }

    @Transactional
    public StartRoundResponse startRound(Long gameRoomId, String email) {
        return totalRoundStartTimer.record(() -> {
            log.info("게임룸 ID로 라운드 시작: {}", gameRoomId);
            log.info("게임룸 검증 및 검색 중.");
            GameRoom gameRoom = em.find(GameRoom.class, gameRoomId, LockModeType.PESSIMISTIC_WRITE);
            log.info("게임룸 검증 및 검색 완료.");

            gameRoom.updateGameState(GameState.START);
            log.info("게임 상태를 START로 업데이트 함.");

            log.info("게임 검색중.");
            Game game = gameRoom.getCurrentGame();
            log.info("Game : {}", game.getId());

            performRoundStartTimer.record(() -> performRoundStart(game));
            Card card = email.equals(game.getPlayerOne().getEmail()) ? game.getPlayerTwoCard() : game.getPlayerOneCard();

            log.info("라운드 시작 작업 수행 완료.");
            int round = game.getRound();

            log.info("게임의 현재 턴 가져오는 중.");
            Turn turn = gameTurnService.getTurn(game.getId());
            log.info("현재 턴 가져옴.");

            int myPoint = email.equals(game.getPlayerOne().getEmail()) ? game.getPlayerOne().getPoints() : game.getPlayerTwo().getPoints();
            int otherPoint = email.equals(game.getPlayerOne().getEmail()) ? game.getPlayerTwo().getPoints() : game.getPlayerOne().getPoints();

            log.info("StartRoundResponse 반환 중.");

            return new StartRoundResponse("ACTION", round, game.getPlayerOne(), game.getPlayerTwo(), card, turn, game.getBetAmount(), game.getPot(), myPoint, otherPoint);
        });
    }

    @Transactional
    public synchronized void performRoundStart(Game game) {
        /* 라운드 수 저장, 라운드 베팅 금액 설정, 플레이어에게 카드 지급, 플레이어 턴 설정*/
        log.info("게임 ID로 라운드 시작 작업 수행 중: {}", game.getId());

        if (!game.isRoundStarted()) {
            game.incrementRound();
            game.updateRoundStarted();
        }

        log.info("라운드가 {}로 증가됨.", game.getRound());

        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        if (game.getPot() == 0) {
            int betAmount = calculateInitialBet(game.getPlayerOne(), game.getPlayerTwo());
            log.info("초기 배팅금액 {}로 설정됨.", betAmount);

            playerOne.decreasePoints(betAmount);
            playerTwo.decreasePoints(betAmount);

            game.setBetAmount(0);
            game.updatePot(betAmount * 2);
        }

        if (game.isCardAllocation() == false) {
            List<Card> availableCards = prepareAvailableCards(game);
            Collections.shuffle(availableCards);
            assignRandomCardsToPlayers(game, availableCards);

            log.info("플레이어에게 카드 할당됨.");
            log.info("{} Card : {}", playerOne.getNickname(), game.getPlayerOneCard());
            log.info("{} Card : {}", playerTwo.getNickname(), game.getPlayerTwoCard());

            game.updateCardAllocation();
        }

        if (game.getRound() == 1) {
            initializeTurnForGame(game);
            log.info("첫 라운드에 턴 초기화 됨.");
        }
    }

    @Transactional
    public int calculateInitialBet(User playerOne, User playerTwo) {
        int playerOnePoints = playerOne.getPoints();
        int playerTwoPoints = playerTwo.getPoints();
        int minPoints = Math.min(playerOnePoints, playerTwoPoints);
        int fivePercentOfMinPoint = (int) Math.round(minPoints * 0.05);
        if (fivePercentOfMinPoint < 1) {
            fivePercentOfMinPoint = 1;
        }
        return Math.min(fivePercentOfMinPoint, 2000);
    }

    private List<Card> prepareAvailableCards(Game game) {
        /* 사용한 카드 목록과 전체 카드 목록을 가져옴
         * 전체 카드 목록에서 사용한 카드 목록을 제외하고 남은 카드 목록을 반환한다*/
        Set<Card> usedCards = game.getUsedCards();
        Set<Card> allCards = EnumSet.allOf(Card.class); // 성능 개선 여지 있음
        allCards.removeAll(usedCards);
        return new ArrayList<>(allCards);
    }

    private void assignRandomCardsToPlayers(Game game, List<Card> availableCards) {
        Card playerOneCard = availableCards.get(0);
        Card playerTwoCard = availableCards.get(1);

        game.setPlayerOneCard(playerOneCard);
        game.setPlayerTwoCard(playerTwoCard);

        game.addUsedCard(playerOneCard);
        game.addUsedCard(playerTwoCard);
    }

    private void initializeTurnForGame(Game game) {
        List<User> players = new ArrayList<>();
        players.add(game.getPlayerOne());
        players.add(game.getPlayerTwo());

        Turn turn = new Turn(players);
        gameTurnService.setTurn(game.getId(), turn);
    }
}
