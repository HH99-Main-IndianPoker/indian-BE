package com.service.indianfrog.domain.gameroom.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionMappingStorage {
    private final Map<String, String> sessionIdToNicknameMap = new ConcurrentHashMap<>();

    public String getNicknameBySessionId(String sessionId) {
        return sessionIdToNicknameMap.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionIdToNicknameMap.remove(sessionId);
    }
}