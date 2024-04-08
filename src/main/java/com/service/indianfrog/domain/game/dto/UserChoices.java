package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.UserChoice;
import lombok.Getter;

@Getter
public class UserChoices {
    private Long gameRoomId;
    private String userOneId;
    private String userTwoId;
    private UserChoice userOneChoice;
    private UserChoice userTwoChoice;

    public UserChoices(Long gameRoomId, String userOneId, String userTwoId,
                       UserChoice userOneChoice, UserChoice userTwoChoice) {

        this.gameRoomId = gameRoomId;
        this.userOneId = userOneId;
        this.userTwoId = userTwoId;
        this.userOneChoice = userOneChoice;
        this.userTwoChoice = userTwoChoice;
    }
}
