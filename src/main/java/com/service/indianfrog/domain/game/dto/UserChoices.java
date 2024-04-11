package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.UserChoice;
import lombok.Getter;

@Getter
public class UserChoices {
    private Long gameRoomId;
    private String userId;
    private UserChoice userChoice;


    public UserChoices(Long gameRoomId, String userId, UserChoice userChoice) {

        this.gameRoomId = gameRoomId;
        this.userId = userId;
        this.userChoice = userChoice;
    }
}
