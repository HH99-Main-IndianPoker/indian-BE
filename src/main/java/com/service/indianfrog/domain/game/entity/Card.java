package com.service.indianfrog.domain.game.entity;

import lombok.Getter;

@Getter
public enum Card {
    DECK1_CARD1(1, 1),
    DECK1_CARD2(1, 2),
    DECK1_CARD3(1, 3),
    DECK1_CARD4(1, 4),
    DECK1_CARD5(1, 5),
    DECK1_CARD6(1, 6),
    DECK1_CARD7(1, 7),
    DECK1_CARD8(1, 8),
    DECK1_CARD9(1, 9),
    DECK1_CARD10(1, 10),
    DECK2_CARD1(2, 1),
    DECK2_CARD2(2, 2),
    DECK2_CARD3(2, 3),
    DECK2_CARD4(2, 4),
    DECK2_CARD5(2, 5),
    DECK2_CARD6(2, 6),
    DECK2_CARD7(2, 7),
    DECK2_CARD8(2, 8),
    DECK2_CARD9(2, 9),
    DECK2_CARD10(2, 10);

    private final int deckNumber;
    private final int number;

    Card(int deckNumber, int number) {
        this.number = number;
        this.deckNumber = deckNumber;
    }
}
