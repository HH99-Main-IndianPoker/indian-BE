package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.UserChoice;
import lombok.Getter;

@Getter
public class UserChoices {
    private String nickname;
    private UserChoice userChoice;


    public UserChoices(Long gameRoomId, String nickname, UserChoice userChoice) {

        this.nickname = nickname;
        this.userChoice = userChoice;
    }
}
