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

    private int pot;

    @ManyToOne(fetch = FetchType.LAZY)
    private User foldedUser;

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
}
