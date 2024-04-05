package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.dto.UserChoices;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.UserChoice;
import com.service.indianfrog.domain.game.utils.GameValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "게임 종료 후 유저 선택 서비스 클래스", description = "게임 종료 후 유저의 선택에 따라 서비스를 수행하는 로직")
@Slf4j
@Service
public class GameSessionService {

    private final GameValidator gameValidator;
    public GameSessionService(GameValidator gameValidator) {
        this.gameValidator = gameValidator;
    }

    public List<GameStatus> processUserChoices(UserChoices choices) {
        gameValidator.validateAndRetrieveGameRoom(choices.getGameRoomId());

        List<GameStatus> statusList = new ArrayList<>();
        if (choices.getUserOneChoice() == UserChoice.PLAY_AGAIN && choices.getUserTwoChoice() == UserChoice.PLAY_AGAIN) {
            statusList.add(startNewGame(choices.getGameRoomId(), choices.getUserOneId()));
            statusList.add(startNewGame(choices.getGameRoomId(), choices.getUserTwoId()));
        } else if (choices.getUserOneChoice() == UserChoice.LEAVE && choices.getUserTwoChoice() == UserChoice.LEAVE) {
            statusList.add(leaveGameRoom(choices.getGameRoomId(), choices.getUserOneId()));
            statusList.add(leaveGameRoom(choices.getGameRoomId(), choices.getUserTwoId()));
        } else {
            if (choices.getUserOneChoice() == UserChoice.PLAY_AGAIN) {
                statusList.add(moveToGameRoom(choices.getGameRoomId(), choices.getUserOneId()));
            } else {
                statusList.add(leaveGameRoom(choices.getGameRoomId(), choices.getUserOneId()));
            }
            if (choices.getUserTwoChoice() == UserChoice.PLAY_AGAIN) {
                statusList.add(moveToGameRoom(choices.getGameRoomId(), choices.getUserTwoId()));
            } else {
                statusList.add(leaveGameRoom(choices.getGameRoomId(), choices.getUserTwoId()));
            }
        }

        return statusList;
    }

    private GameStatus startNewGame(Long gameRoomId, String userId) {
        return new GameStatus(gameRoomId, userId, GameState.START);
    }

    private GameStatus leaveGameRoom(Long gameRoomId, String userId) {
        return new GameStatus(gameRoomId, userId, GameState.LEAVE);
    }

    private GameStatus moveToGameRoom(Long gameRoomId, String userId) {
        return new GameStatus(gameRoomId, userId, GameState.ENTER);
    }
}
