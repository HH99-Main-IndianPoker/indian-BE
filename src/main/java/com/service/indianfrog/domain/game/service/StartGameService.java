package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameDto.StartRoundResponse;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameRoom;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public StartRoundResponse startRound(Long gameRoomId) {
        GameRoom gameRoom = gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        Game game = gameValidator.initializeOrRetrieveGame(gameRoom);

        performRoundStart(game);
        gameValidator.saveGameRoomState(gameRoom);
        int round = game.getRound();

        return new StartRoundResponse("ACTION", round);
    }

    private void performRoundStart(Game game) {
        /* 라운드 수 저장, 라운드 베팅 금액 설정, 플레이어에게 카드 지급, 플레이어 턴 설정*/
        game.incrementRound();

        int betAmount = calculateInitialBet(game.getPlayerOne(), game.getPlayerTwo());
        game.setBetAmount(betAmount);

        List<Card> availableCards = prepareAvailableCards(game);
        assignRandomCardsToPlayers(game, availableCards);

        initializeTurnForGame(game);
    }

    private int calculateInitialBet(User playerOne, User playerTwo) {
        int playerOnePoints = playerOne.getPoints();
        int playerTwoPoints = playerTwo.getPoints();
        int lowerPoints = Math.min(playerOnePoints, playerTwoPoints);
        return lowerPoints / 10; // 10%의 포인트를 초기 베팅 금액으로 설정
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
