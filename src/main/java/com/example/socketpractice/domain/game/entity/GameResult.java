package com.example.socketpractice.domain.game.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class GameResult {

    @Id
    private Long gameResultId;

    private Long userId;

    private Long roomId;
}
