package com.service.indianfrog.domain.gameroom.entity;

import com.rabbitmq.client.LongString;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "validate_room")
@NoArgsConstructor
public class ValidateRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "valid_id")
    private Long validId;

    @Column(name = "participant")
    private String participants;

    @Column(name = "host")
    private boolean host;

    @Column(name = "ready")
    private boolean ready;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom gameRoom;


    @Builder
    public ValidateRoom(String participants, boolean host, boolean ready, GameRoom gameRoom) {
        this.participants = participants;
        this.host = host;
        this.ready = ready;
        this.gameRoom = gameRoom;
    }

    public void revert(boolean isReady){
        this.ready = !isReady;
    }

    public void updateHost() {
        this.host = true;
    }

}