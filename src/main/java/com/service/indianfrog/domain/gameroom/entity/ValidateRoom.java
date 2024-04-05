package com.service.indianfrog.domain.gameroom.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "validate_room")
public class ValidateRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "valid_id")
    Long validId;
    @Column(name = "participants")
    String participants;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom gameRoom;

    public void setValidId(Long validId) {
        this.validId = validId;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }
}
