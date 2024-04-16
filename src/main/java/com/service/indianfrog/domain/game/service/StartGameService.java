package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameDto.StartRoundResponse;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.service.GameRoomService;
import com.service.indianfrog.domain.user.entity.User;
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

    public StartGameService(GameValidator gameValidator, GameTurnService gameTurnService) {
        this.gameValidator = gameValidator;
        this.gameTurnService = gameTurnService;
    }

    @Transactional
    public StartRoundResponse startRound(Long gameRoomId) {
        log.debug("게임룸 ID로 라운드 시작: {}", gameRoomId);

        log.debug("게임룸 검증 및 검색 중.");
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        log.debug("게임룸 검증 및 검색 완료.");

        gameRoom.updateGameState(GameState.START);
        log.debug("게임 상태를 START로 업데이트 함.");

        log.debug("게임 초기화 또는 검색 중.");
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);
        log.debug("게임 초기화 또는 검색 완료.");

        performRoundStart(game);
        log.debug("라운드 시작 작업 수행 완료.");

        gameValidator.saveGameRoomState(gameRoom);
        log.debug("게임룸 상태 저장 완료.");

        int round = game.getRound();

        log.debug("게임의 현재 턴 가져오는 중.");
        Turn turn = gameTurnService.getTurn(game.getId());
        log.debug("현재 턴 가져옴.");

        log.debug("StartRoundResponse 반환 중.");
        return new StartRoundResponse("ACTION", round, game.getPlayerOne(), game.getPlayerTwo(),
                game.getPlayerOneCard(), game.getPlayerTwoCard(), turn);
    }

    private void performRoundStart(Game game) {
        /* 라운드 수 저장, 라운드 베팅 금액 설정, 플레이어에게 카드 지급, 플레이어 턴 설정*/
        log.debug("게임 ID로 라운드 시작 작업 수행 중: {}", game.getId());

        game.incrementRound();
        log.debug("라운드가 {}로 증가됨.", game.getRound());

        int betAmount = calculateInitialBet(game.getPlayerOne(), game.getPlayerTwo());
        game.setBetAmount(betAmount);
        log.debug("베팅 금액이 {}로 설정됨.", betAmount);

        List<Card> availableCards = prepareAvailableCards(game);
        assignRandomCardsToPlayers(game, availableCards);
        log.debug("플레이어에게 카드 할당됨.");

        if (game.getRound() == 1) {
            initializeTurnForGame(game);
            log.debug("첫 라운드에 턴 초기화 됨.");
        }
    }

    private int calculateInitialBet(User playerOne, User playerTwo) {
        int playerOnePoints = playerOne.getPoints();
        int playerTwoPoints = playerTwo.getPoints();
        int minPoints = Math.min(playerOnePoints, playerTwoPoints);
        int tenPercentOfMinPoints = (int) (minPoints * 0.1);
        return Math.min(tenPercentOfMinPoints, 2000);
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
