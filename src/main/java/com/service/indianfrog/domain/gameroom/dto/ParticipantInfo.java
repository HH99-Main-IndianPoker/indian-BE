package com.service.indianfrog.domain.gameroom.dto;

import lombok.Getter;

@Getter
public class ParticipantInfo {
    private String participant;
    private String host;
    private int participantPoint;
    private int hostPoint;
    private String participantImgUrl;
    private String hostImgUrl;

    public ParticipantInfo(String participant, String host, int participantPoint, int hostPoint, String participantImgUrl, String hostImgUrl) {
        this.participant = participant;
        this.host = host;
        this.participantPoint = participantPoint;
        this.hostPoint = hostPoint;
        this.participantImgUrl = participantImgUrl;
        this.hostImgUrl = hostImgUrl;
    }

}
