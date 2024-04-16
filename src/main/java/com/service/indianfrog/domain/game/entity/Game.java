package com.service.indianfrog.domain.game.entity;

import com.service.indianfrog.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.EAGER)
    private User playerOne;

    @ManyToOne(fetch = FetchType.EAGER)
    private User playerTwo;

    @Enumerated(EnumType.STRING) // 카드 Enum을 저장
    private Card playerOneCard;

    @Enumerated(EnumType.STRING) // 카드 Enum을 저장
    private Card playerTwoCard;

    private int betAmount;

    private int pot; // 현재 라운드의 포트
    private int nextRoundPot; // 다음 라운드로 이월할 포트

    @ManyToOne(fetch = FetchType.EAGER)
    private User foldedUser;

    // 플레이어가 라운드에서 획득한 포인트
    private int playerOneRoundPoints;
    private int playerTwoRoundPoints;

    // 라운드 정보
    private int round;

    private boolean checkStatus;
    private boolean raiseStatus;

    // Constructor and methods
    public Game(User playerOne, User playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.usedCards = new HashSet<>();
    }

    public void addUsedCard(Card card) {
        this.usedCards.add(card);
    }

    public void incrementRound() {
        this.round++;
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
        /* 라운드 정보 초기화
         * 베팅액, 각 플레이어 카드 정보 초기화*/
        this.pot = 0;
        this.playerOneCard = null;
        this.playerTwoCard = null;
    }

    // 게임과 관련된 상태를 초기화하는 메서드
    public void resetGame() {
        /* 게임에 사용된 카드 정보,
         * 게임에서 각 유저가 획득한 포인트,
         * 라운드 정보 초기화*/
        usedCards.clear();
        playerOneRoundPoints = 0;
        playerTwoRoundPoints = 0;
        round = 0;
    }

    public void updateCheck() {
        this.checkStatus = true;
    }

    public void updateRaise() {
        this.raiseStatus = true;
    }
}
