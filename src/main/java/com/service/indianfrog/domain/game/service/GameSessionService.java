package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.dto.UserChoices;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Tag(name = "게임 종료 후 유저 선택 서비스 클래스", description = "게임 종료 후 유저의 선택에 따라 서비스를 수행하는 로직")
@Slf4j
@Service
public class GameSessionService {

    private final GameValidator gameValidator;

    public GameSessionService(GameValidator gameValidator) {
        this.gameValidator = gameValidator;
    }

    private final Map<Long, Map<String, String>> gameChoices = new ConcurrentHashMap<>();

    @Transactional
    public Object processUserChoices(Long gameRoomId, UserChoices choices) {
        /* 입력 값 검증*/
        gameValidator.validateAndRetrieveGameRoom(gameRoomId);
        User player = gameValidator.findUserByNickname(choices.getNickname());

        String nickname = player.getNickname();
        String choice = choices.getUserChoice().toString();

        /* 유저 선택 저장*/
        gameChoices.computeIfAbsent(gameRoomId, k -> new ConcurrentHashMap<>()).put(nickname, choice);

        /* 모든 유저의 선택이 완료되었는지 확인*/
        if (gameChoices.get(gameRoomId).size() == 2) {
            return new GameStatus(gameRoomId, nickname, determineActionAndProceed(gameRoomId));
        }

        return "다른 플레이어의 선택을 기다려주세요";
    }

    private GameState determineActionAndProceed(Long gameRoomId) {
        Map<String, String> roomChoices = gameChoices.get(gameRoomId);
        boolean allSame = new HashSet<>(roomChoices.values()).size() == 1;
        gameChoices.remove(gameRoomId);

        if (allSame) {
            return roomChoices.values().iterator().next().equals("PLAY_AGAIN") ? GameState.START : GameState.LEAVE;
        } else {
            return GameState.ENTER;
        }
    }
}
