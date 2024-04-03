package com.example.socketpractice.domain.game.entity;

import com.example.socketpractice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING) // Enum 타입을 저장
    private Set<Card> usedCards = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User playerOne;

    @ManyToOne(fetch = FetchType.LAZY)
    private User playerTwo;

    @Enumerated(EnumType.STRING) // 카드 Enum을 저장
    private Card playerOneCard;

    @Enumerated(EnumType.STRING) // 카드 Enum을 저장
    private Card playerTwoCard;

    private int betAmount;

    private int pot; // 현재 라운드의 포트
    private int nextRoundPot; // 다음 라운드로 이월할 포트

    @ManyToOne(fetch = FetchType.LAZY)
    private User foldedUser;

    // 플레이어가 라운드에서 획득한 포인트
    private int playerOneRoundPoints;
    private int playerTwoRoundPoints;

    // Constructor and methods
    public Game(User playerOne, User playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.usedCards = new HashSet<>();
    }

    public void addUsedCard(Card card) {
        this.usedCards.add(card);
    }

    public void setPlayerOneCard(Card card) {
        this.playerOneCard = card;
    }

    public void setPlayerTwoCard(Card card) {
        this.playerTwoCard = card;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }
    // 게임 팟을 가져옵니다.
    public int getPot() {
        return pot;
    }

    // 게임 팟을 설정합니다.
    public void setPot(int pot) {
        this.pot = pot;
    }

    // 게임에서 포기한 유저를 설정합니다.
    public void setFoldedUser(User user) {
        this.foldedUser = user;
    }

    public void addPlayerOneRoundPoints(int points) {
        this.playerOneRoundPoints += points;
    }

    public void addPlayerTwoRoundPoints(int points) {
        this.playerTwoRoundPoints += points;
    }

    public void setNextRoundPot(int pot) {
        // 다음 라운드로 이월할 포트 금액을 설정합니다.
        this.nextRoundPot += pot; // 이월될 금액을 누적합니다.
    }

    public void resetRound() {
        // 라운드 관련 정보를 초기화하는 메서드
        // 카드를 초기화하고, 포트를 다음 라운드로 이월하며, 현재 라운드의 포트를 다음 라운드의 포트로 설정합니다.
        this.pot = this.nextRoundPot; // 다음 라운드로 이월된 포트 금액을 현재 포트로 설정합니다.
        this.nextRoundPot = 0; // 다음 라운드 포트 초기화
        // 추가로 필요한 라운드 관련 정보 초기화 로직을 여기에 구현합니다.
    }

    // 게임과 관련된 상태를 초기화하는 메서드
    public void resetGame() {
        // 사용된 카드 목록을 비우고, 각 플레이어의 라운드별 획득 포인트를 0으로 리셋합니다.
        usedCards.clear();
        playerOneRoundPoints = 0;
        playerTwoRoundPoints = 0;

        // 초기 베팅 금액과 팟을 리셋합니다.
        pot = 0;
        nextRoundPot = 0;

        // 각 플레이어의 카드를 null 또는 초기 상태로 설정할 수 있습니다.
        // 예를 들어, 플레이어의 카드 필드가 있다면 이를 초기화합니다.
        // playerOneCard = null;
        // playerTwoCard = null;

        // 기타 필요한 상태 초기화 로직
        // 예: 라운드 수, 게임의 상태, 시간 제한 등을 초기화할 수 있습니다.
    }
}
