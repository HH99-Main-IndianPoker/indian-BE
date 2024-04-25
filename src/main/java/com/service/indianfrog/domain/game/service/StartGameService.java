package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameDto.StartRoundResponse;
import com.service.indianfrog.domain.game.entity.Card;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Tag(name = "라운드 시작 서비스", description = "게임(라운드) 시작 서비스 로직")
@Slf4j(topic = "게임 시작 서비스 레이어")
@Service
public class StartGameService {

    /* 생성자를 통한 필드 주입 */
    private final GameValidator gameValidator;
    private final GameTurnService gameTurnService;
    private final GameRepository gameRepository;
    private final Timer totalRoundStartTimer;
    private final Timer performRoundStartTimer;

    public StartGameService(GameValidator gameValidator, GameTurnService gameTurnService, GameRepository gameRepository, MeterRegistry registry) {
        this.gameValidator = gameValidator;
        this.gameTurnService = gameTurnService;
        this.gameRepository = gameRepository;
        this.totalRoundStartTimer = registry.timer("totalRoundStart.time");
        this.performRoundStartTimer = registry.timer("performRoundStart.time");
    }

    @Transactional
    public StartRoundResponse startRound(Long gameRoomId) {
        return totalRoundStartTimer.record(() -> {
            log.info("게임룸 ID로 라운드 시작: {}", gameRoomId);
            log.info("게임룸 검증 및 검색 중.");
            GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
            log.info("게임룸 검증 및 검색 완료.");

            int firstBet = 0;
            int round = 0;
            Turn turn = null;
            Game game;

            if (gameRoom.getGameState().equals(GameState.READY)) {
                gameRoom.updateGameState(GameState.START);
                log.info("게임 상태를 START로 업데이트 함.");

                log.info("게임 초기화 또는 검색 중.");
                game = gameValidator.initializeOrRetrieveGame(gameRoom);
                log.info("게임 초기화 또는 검색 완료.");

                firstBet = performRoundStartTimer.record(() -> performRoundStart(game));

                log.info("라운드 시작 작업 수행 완료.");
                round = game.getRound();

                gameValidator.saveGameRoomState(gameRoom);
                log.info("게임룸 상태 저장 완료.");

                log.info("게임의 현재 턴 가져오는 중.");
                turn = gameTurnService.getTurn(game.getId());
                log.info("현재 턴 가져옴.");
            } else {
                game = gameRoom.getCurrentGame();
            }

            if (gameRoom.getGameState().equals(GameState.START)){
                firstBet = game.getBetAmount();

                round = game.getRound();

                log.info("게임의 현재 턴 가져오는 중.");
                turn = gameTurnService.getTurn(game.getId());
                log.info("현재 턴 가져옴.");
            }

            log.info("StartRoundResponse 반환 중.");

            return new StartRoundResponse("ACTION", round, game.getPlayerOne(), game.getPlayerTwo(),
                    game.getPlayerOneCard(), game.getPlayerTwoCard(), turn, firstBet);
        });
    }

    private int performRoundStart(Game game) {
        /* 라운드 수 저장, 라운드 베팅 금액 설정, 플레이어에게 카드 지급, 플레이어 턴 설정*/
        log.info("게임 ID로 라운드 시작 작업 수행 중: {}", game.getId());

        game.incrementRound();
        log.info("라운드가 {}로 증가됨.", game.getRound());

        int betAmount = calculateInitialBet(game.getPlayerOne(), game.getPlayerTwo());
        log.info("초기 배팅금액 {}로 설정됨.", betAmount);

        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        // 이거 왜 유저 포인트 - betAmount 하는 걸까?
        playerOne.updatePoint(betAmount);
        playerTwo.updatePoint(betAmount);

        game.setBetAmount(0);
        game.setPot(betAmount * 2);

        List<Card> availableCards = prepareAvailableCards(game);
        assignRandomCardsToPlayers(game, availableCards);
        log.info("플레이어에게 카드 할당됨.");

        gameRepository.save(game);

        if (game.getRound() == 1) {
            initializeTurnForGame(game);
            log.info("첫 라운드에 턴 초기화 됨.");
        }

        return betAmount;
    }

    private int calculateInitialBet(User playerOne, User playerTwo) {
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
        /* 카드를 섞은 후 플레이어에게 각각 한장 씩 제공
         * 플레이어에게 제공한 카드는 사용한 카드목록에 포함되어 다음 라운드에서는 사용되지 않는다*/
        Collections.shuffle(availableCards);

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
