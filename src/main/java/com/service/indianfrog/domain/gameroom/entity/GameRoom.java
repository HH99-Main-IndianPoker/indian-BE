package com.service.indianfrog.domain.gameroom.entity;


import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "Game_room")
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;
    @Column(name = "create_at")
    private Date createAt;
    @Column(name = "room_name")
    private String roomName;

    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ValidateRoom> validateRooms = new HashSet<>();

    /* 유저 및 게임 관련*/
    @ManyToOne(fetch = FetchType.LAZY)
    private User playerOne;
    @ManyToOne(fetch = FetchType.LAZY)
    private User playerTwo;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Game currentGame;

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void startNewGame(User playerOne, User playerTwo) {
        this.currentGame = new Game(playerOne, playerTwo);
    }

    public void setCurrentGame(Game game) {
        this.currentGame = game;
    }

    // 게임을 종료할 때 호출하는 메서드입니다.
    public void endCurrentGame() {
        this.currentGame = null;
    }
}