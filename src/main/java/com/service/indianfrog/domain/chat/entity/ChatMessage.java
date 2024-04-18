package com.service.indianfrog.domain.chat.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatMessage {
    private String content;
    private String sender;
    private int point;
    private String senderImgUrl;
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    @Builder
    public ChatMessage(MessageType type, String content, String sender, int points, String imgUrl) {
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.point = points;
        this.senderImgUrl = imgUrl;
    }
}
