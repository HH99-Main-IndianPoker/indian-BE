package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Tag(name = "게임 내 턴 관리 서비스 로직")
@Slf4j
@Service
public class GameTurnService {

    private final Map<Long, Turn> gameTurns = new ConcurrentHashMap<>();

    public void setTurn(Long gameId, Turn turn) {
        gameTurns.put(gameId, turn);
    }

    public Turn getTurn(Long gameId) {
        return gameTurns.get(gameId);
    }


}
