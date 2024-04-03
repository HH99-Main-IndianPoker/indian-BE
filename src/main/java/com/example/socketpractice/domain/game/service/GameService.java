package com.example.socketpractice.domain.game.service;

import com.example.socketpractice.domain.game.entity.Card;
import com.example.socketpractice.domain.game.entity.Game;
import com.example.socketpractice.domain.game.entity.GameRoom;
import com.example.socketpractice.domain.game.entity.UserChoice;
import com.example.socketpractice.domain.game.repository.GameRepository;
import com.example.socketpractice.domain.game.repository.GameRoomRepository;
import com.example.socketpractice.domain.user.entity.User;
import com.example.socketpractice.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;
    private static final EnumSet<Card> ALL_CARDS = EnumSet.allOf(Card.class);

    public GameService(GameRepository gameRepository, GameRoomRepository gameRoomRepository, UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public void playerAction(Long gameRoomId, String nickname, String gameState) {
        // 플레이어 행동 처리(채팅, 배팅)
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Game room not found with ID: " + gameRoomId));

        Game game = gameRoom.getCurrentGame();
        if (game == null) {
            throw new IllegalStateException("Game not started or already ended.");
        }

        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + nickname));

        switch (gameState.toUpperCase()) {
            case "CHECK":
                performCheckAction(game, user);
                break;
            case "RAISE":
                performRaiseAction(game, user);
                break;
            case "DIE":
                performDieAction(game, user);
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + gameState);
        }

        gameRoomRepository.save(gameRoom);
    }

    @Transactional
    public void endRound(Long gameRoomId) {
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Game room not found with ID: " + gameRoomId));

        Game game = gameRoom.getCurrentGame();
        if (game == null) {
            throw new IllegalStateException("No game is currently active in this room.");
        }

        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();
        Card playerOneCard = game.getPlayerOneCard();
        Card playerTwoCard = game.getPlayerTwoCard();

        int pot = game.getPot(); // 이번 라운드의 팟
        User roundWinner = determineWinner(playerOne, playerOneCard, playerTwo, playerTwoCard);

        if (roundWinner != null) {
            if (roundWinner.equals(playerOne)) {
                game.addPlayerOneRoundPoints(pot);
            } else {
                game.addPlayerTwoRoundPoints(pot);
            }
        } else {
            // 무승부인 경우, 다음 라운드로 팟을 이월합니다.
            game.setNextRoundPot(pot);
        }

        // 라운드 정보를 초기화하는 로직을 추가합니다. 예를 들어, 카드 초기화, 팟 초기화 등
        game.resetRound();

        gameRoomRepository.save(gameRoom);
    }


    @Transactional
    public void endGame(Long gameRoomId) {
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Game room not found with ID: " + gameRoomId));

        Game game = gameRoom.getCurrentGame();
        if (game == null) {
            throw new IllegalStateException("No game is currently active in this room.");
        }

        // 게임의 결과를 가져오고, 게임 관련 데이터를 초기화합니다.
        processGameResults(gameRoom);

        // 사용자의 선택을 받아 상태를 결정합니다.
        processUserChoices(gameRoom);
    }

    private int calculateInitialBet(User playerOne, User playerTwo) {
        int playerOnePoints = playerOne.getPoints();
        int playerTwoPoints = playerTwo.getPoints();
        int lowerPoints = Math.min(playerOnePoints, playerTwoPoints);
        return lowerPoints / 10; // 10%의 포인트를 초기 베팅 금액으로 설정
    }

    private void performCheckAction(Game game, User user) {
        // 첫 번째 플레이어가 '체크'할 경우 현재 베팅 금액에 변화를 주지 않습니다.
        boolean isFirstPlayer = game.getPlayerOne().equals(user);
        if (!isFirstPlayer) {
            // 두 번째 플레이어는 현재 베팅 금액을 팟에 추가합니다.
            int userPoints = user.getPoints();
            int currentBet = game.getBetAmount();
            if (userPoints >= currentBet) {
                user.setPoints(userPoints - currentBet); // 유저의 포인트를 감소시킵니다.
                game.setPot(game.getPot() + currentBet); // 팟을 증가시킵니다.
            } else {
                // 유저의 포인트가 현재 베팅 금액보다 적다면 예외를 발생시킵니다.
                throw new IllegalStateException("User does not have enough points to check.");
            }
        }
    }

    private void performRaiseAction(Game game, User user) {
        int raiseAmount = game.getBetAmount() * 2;
        int userPoints = user.getPoints();

        // The user can only raise if they have enough points.
        if (userPoints >= raiseAmount) {
            game.setBetAmount(raiseAmount);
            user.setPoints(userPoints - raiseAmount);
        } else {
            throw new IllegalStateException("User does not have enough points to raise.");
        }
    }

    private void performDieAction(Game game, User user) {
        // The user forfeits the round and possibly the game.
        game.setFoldedUser(user);
    }

    private static User getWinner(Game game) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        int playerOneTotalPoints = game.getPlayerOneRoundPoints();
        int playerTwoTotalPoints = game.getPlayerTwoRoundPoints();

        User gameWinner;
        if (playerOneTotalPoints > playerTwoTotalPoints) {
            gameWinner = playerOne;
            playerOne.incrementWins();
            playerTwo.incrementLosses();
        } else if (playerTwoTotalPoints > playerOneTotalPoints) {
            gameWinner = playerTwo;
            playerTwo.incrementWins();
            playerOne.incrementLosses();
        } else {
            // 무승부 처리
            gameWinner = null;
        }
        return gameWinner;
    }

    private User determineWinner(User playerOne, Card playerOneCard, User playerTwo, Card playerTwoCard) {
        // 두 플레이어의 카드를 비교하여 승자를 결정하는 로직
        // 예시 로직을 사용하여 승자를 결정합니다. 실제 게임 룰에 맞게 수정할 필요가 있습니다.
        if (playerOneCard.getNumber() > playerTwoCard.getNumber()) {
            return playerOne;
        } else if (playerOneCard.getNumber() < playerTwoCard.getNumber()) {
            return playerTwo;
        } else {
            // 무승부인 경우
            return null;
        }

    }

    private void processGameResults(GameRoom gameRoom) {
        Game game = gameRoom.getCurrentGame();

        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        int playerOneTotalPoints = game.getPlayerOneRoundPoints();
        int playerTwoTotalPoints = game.getPlayerTwoRoundPoints();

        // 승자 결정
        User gameWinner = getWinner(game);

        // 게임 데이터 초기화
        game.resetGame();

        // 게임 룸 업데이트
        // 이 부분은 gameWinner의 값에 따라 게임 룸의 상태를 업데이트할 수도 있습니다.
        // 예를 들어, 게임의 결과를 기록하거나 특정 게임 룸 설정을 변경할 수 있습니다.

        // 승자가 결정된 경우 추가 처리
        if (gameWinner != null) {
            // 승자의 승리 횟수를 데이터베이스에 반영합니다.
            userRepository.save(gameWinner);
            // 게임 룸의 상태를 '대기 중'이나 '게임 종료' 등 적절한 상태로 업데이트할 수 있습니다.
        }

        // 무승부인 경우의 처리 로직도 여기에 포함될 수 있습니다.
        // 예를 들어, 게임 룸을 계속 활성 상태로 두거나, 무승부를 사용자에게 알릴 수 있습니다.
    }

    public void processUserChoices(GameRoom gameRoom) {
        // 이 예제에서는 사용자의 선택을 특정 저장소나 상태에서 가져오는 것으로 가정합니다.
        // 실제로는 사용자의 선택을 관리하는 별도의 로직이 필요합니다.

        // 사용자의 선택에 따라 게임방의 상태를 결정하고 조치를 취합니다.
        UserChoice playerOneChoice = getUserChoice(gameRoom.getPlayerOne());
        UserChoice playerTwoChoice = getUserChoice(gameRoom.getPlayerTwo());

        if (playerOneChoice == UserChoice.PLAY_AGAIN && playerTwoChoice == UserChoice.PLAY_AGAIN) {
            // 둘 다 다시 하기를 선택한 경우, 게임방을 재설정하고 새 게임을 시작합니다.
            gameRoom.startNewGame(gameRoom.getPlayerOne(), gameRoom.getPlayerTwo());
        } else if (playerOneChoice == UserChoice.LEAVE && playerTwoChoice == UserChoice.LEAVE) {
            // 둘 다 나가기를 선택한 경우, 게임방을 종료하고 삭제합니다.
            gameRoomRepository.delete(gameRoom);
        } else {
            // 한 명만 게임을 계속하려는 경우
            if (playerOneChoice == UserChoice.PLAY_AGAIN) {
                // Player One는 게임방으로 이동, Player Two는 로비로 이동
            } else {
                // Player Two는 게임방으로 이동, Player One은 로비로 이동
            }
            // 여기에서는 해당 플레이어를 로비로 보내는 로직을 구현해야 합니다.
        }
    }

    private UserChoice getUserChoice(User player) {
        // 이 메서드는 사용자의 선택을 반환합니다.
        // 예제에서는 선택을 바로 반환하고 있지만, 실제로는 사용자가 선택한 내용을 어딘가에서 가져와야 합니다.
        // 예: 데이터베이스, 세션, 캐시 등
        return UserChoice.PLAY_AGAIN; // 예시로 항상 'PLAY_AGAIN'을 반환하고 있습니다.
    }

}