package com.service.indianfrog.domain.gameroom.entity;


import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "Game_room")
public class GameRoom extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name")
    private String roomName;

    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 5) //혹시 몰라서 2로 안하고 5로 함.
    @Fetch(FetchMode.SUBSELECT)
    private Set<ValidateRoom> validateRooms = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id")
    private Game currentGame;

    @Enumerated(EnumType.STRING)
    private GameState gameState;

    @Builder
    public GameRoom(Long roomId, String roomName, Set<ValidateRoom> validateRooms, Game currentGame, GameState gameState) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.validateRooms = validateRooms;
        this.currentGame = currentGame;
        this.gameState = gameState;

    }

    public GameRoom() {

    }

    public void startNewGame(Game game) {
        this.currentGame = game;
    }

    public void updateGameState(GameState gameState) {
        this.gameState = gameState;
    }
}