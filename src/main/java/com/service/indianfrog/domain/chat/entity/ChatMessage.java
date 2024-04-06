package com.service.indianfrog.domain.chat.entity;

import lombok.Getter;

@Getter

public class ChatMessage {
    private String content;
    private String sender;

    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
