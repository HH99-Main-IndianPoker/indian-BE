package com.service.indianfrog.domain.game.dto;

public class GameRequestDto {
    public record GameBetting(
            String action,
            String nickname,
            int point
    ) {}

}
