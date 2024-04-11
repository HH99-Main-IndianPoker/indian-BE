package com.service.indianfrog.domain.gameroom.util;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class SessionMappingStorage {
    private final Map<String, String> sessionIdToNicknameMap = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String nickname) {
        sessionIdToNicknameMap.put(sessionId, nickname);
    }

    public String getNicknameBySessionId(String sessionId) {
        return sessionIdToNicknameMap.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionIdToNicknameMap.remove(sessionId);
    }
}