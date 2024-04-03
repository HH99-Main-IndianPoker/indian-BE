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

}
