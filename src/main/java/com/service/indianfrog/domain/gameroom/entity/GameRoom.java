package com.service.indianfrog.domain.gameroom.entity;


import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "Game_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRoom extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name")
    private String roomName;

    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ValidateRoom> validateRooms = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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

    public void startNewGame(User playerOne, User playerTwo) {
        this.currentGame = new Game(playerOne, playerTwo);
    }


    // 게임을 종료할 때 호출하는 메서드입니다.
    public void endCurrentGame() {
        this.currentGame = null;
    }

    public void updateGameState(GameState gameState) {
        this.gameState = gameState;
    }
}