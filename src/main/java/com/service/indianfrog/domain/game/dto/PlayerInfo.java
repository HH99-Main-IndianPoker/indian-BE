package com.service.indianfrog.domain.game.dto;

import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.user.entity.User;
import lombok.Getter;

@Getter
public class PlayerInfo {
    private String id;
    private Card card;

    public PlayerInfo(User player, Card card) {
        this.id = player.getNickname();
        this.card = card;
    }
}
