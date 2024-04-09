package com.service.indianfrog.domain.game.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Deck {

    @Id
    private Long deckId;
}
