package com.example.socketpractice.domain.game.service;

import com.example.socketpractice.domain.game.entity.Card;
import com.example.socketpractice.domain.game.entity.Game;
import com.example.socketpractice.domain.game.entity.GameRoom;
import com.example.socketpractice.domain.game.repository.GameRepository;
import com.example.socketpractice.domain.game.repository.GameRoomRepository;
import com.example.socketpractice.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameRoomRepository gameRoomRepository;
    private static final EnumSet<Card> ALL_CARDS = EnumSet.allOf(Card.class);
    public GameService(GameRepository gameRepository, GameRoomRepository gameRoomRepository) {
        this.gameRepository = gameRepository;
        this.gameRoomRepository = gameRoomRepository;
    }

    /* 게임 실행 관련 로직
    * 1. 라운드 시작 로직
    * 2. 플레이어 행동 처리 로직 - 채팅, 배팅
    * 3. 라운드 종료 로직
    * 4. 게임 종료 로직 */

    public void startRound(Long gameRoomId) {
        /* gameRoomId 사용 게임 룸 정보 검증 및 게임 인스턴스 확인 및 생성*/
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Game room not found with ID: " + gameRoomId));

        Game game = gameRoom.getCurrentGame();

        if (game == null) {
            gameRoom.startNewGame(gameRoom.getPlayerOne(), gameRoom.getPlayerTwo());
            game = gameRoom.getCurrentGame();
            gameRoomRepository.save(gameRoom);
        }

        /* 게임 라운드 시작 로직*/
        // 이전 라운드에서 사용된 카드를 제외한 카드 목록을 생성합니다.
        Set<Card> usedCards = game.getUsedCards();
        List<Card> availableCards = new ArrayList<>(EnumSet.complementOf(EnumSet.copyOf(usedCards)));

        // 카드를 섞습니다.
        Collections.shuffle(availableCards);

        // 랜덤 카드를 플레이어에게 할당합니다.
        Card playerOneCard = availableCards.get(0);
        Card playerTwoCard = availableCards.get(1);
        game.setPlayerOneCard(playerOneCard);
        game.setPlayerTwoCard(playerTwoCard);

        // 사용된 카드를 추적합니다.
        game.addUsedCard(playerOneCard);
        game.addUsedCard(playerTwoCard);

        // 초기 베팅 금액을 계산하고 설정합니다.
        int betAmount = calculateInitialBet(game.getPlayerOne(), game.getPlayerTwo());
        game.setBetAmount(betAmount);

        // 변경사항을 데이터베이스에 저장합니다.
        gameRoomRepository.save(gameRoom);
        // 게임 상태 업데이트, 채팅 시간 관리는 클라이언트 단에서 처리
    }

    public void playerAction(Long gameRoomId, String username, String gameState) {
        // 플레이어 행동 처리(채팅, 배팅)
    }

    public void endRound(Long gameRoomId) {
        // 라운드 종료 로직
    }

    public void endGame(Long gameRoomId) {
        // 게임 종료 로직
    }

    private int calculateInitialBet(User playerOne, User playerTwo) {
        int playerOnePoints = playerOne.getPoints();
        int playerTwoPoints = playerTwo.getPoints();
        int lowerPoints = Math.min(playerOnePoints, playerTwoPoints);
        return lowerPoints / 10; // 10%의 포인트를 초기 베팅 금액으로 설정
    }
}
