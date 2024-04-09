package com.service.indianfrog.domain.gameroom.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "validate_room")
public class ValidateRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "valid_id")
    private Long validId;

    @Column(name = "participant")
    private String participants;

    @Column(name = "host")
    private boolean host;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom gameRoom;


    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public void setHost(boolean host) {
        this.host = host;
    }


}
