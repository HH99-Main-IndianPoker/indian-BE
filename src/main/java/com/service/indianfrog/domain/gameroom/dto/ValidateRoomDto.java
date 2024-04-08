package com.service.indianfrog.domain.gameroom.dto;

import lombok.Getter;

@Getter
public class ValidateRoomDto {
    private Long validId;
    private String participant;


    public ValidateRoomDto(Long validId, String participant) {
        this.validId = validId;
        this.participant = participant;
    }

}
