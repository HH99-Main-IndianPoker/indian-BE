package com.example.socketpractice.domain.game.entity;

import com.example.socketpractice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

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
}