package com.service.indianfrog.domain.gameroom.util;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class SessionMappingStorage {
    private final Map<String, String> sessionIdToEmailMap = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String email) {
        sessionIdToEmailMap.put(sessionId, email);
    }

    public String getEmailBySessionId(String sessionId) {
        return sessionIdToEmailMap.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionIdToEmailMap.remove(sessionId);
    }
}