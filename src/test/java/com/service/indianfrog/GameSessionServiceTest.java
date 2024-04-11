package com.service.indianfrog;

import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.dto.UserChoices;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.UserChoice;
import com.service.indianfrog.domain.game.service.GameSessionService;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GameSessionServiceTest {

    @Mock
    private GameValidator gameValidator;

    private GameSessionService gameSessionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameSessionService = new GameSessionService(gameValidator);
    }

    @Test
    @DisplayName("게임 종료 후 유저 선택 - 게임 재시작 성공 테스트")
    @Disabled
    public void testProcessUserChoicesBothPlayAgain() {
        Long gameRoomId = 1L;
        GameRoom gameRoom = mock(GameRoom.class);
        String userOneId = "1";
        String userTwoId = "2";
        UserChoices choice1 = new UserChoices(gameRoomId, userOneId, UserChoice.PLAY_AGAIN);
        UserChoices choice2 = new UserChoices(gameRoomId, userTwoId, UserChoice.PLAY_AGAIN);

        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(gameRoom);

        Object result1 = gameSessionService.processUserChoices(choice1);
        Object result2 = gameSessionService.processUserChoices(choice2);

        assertInstanceOf(GameStatus.class, result2); // 두 번째 선택 결과가 GameStatus 여야 함
        assertEquals(GameState.START, ((GameStatus) result2).getGameState()); // 두 번째 선택 결과 상태 검증
    }

    @Test
    @DisplayName("게임 종료 후 유저 선택 - 게임 나가기 성공 테스트")
    @Disabled
    void testProcessUserChoicesBothLeave() {
        Long gameRoomId = 1L;
        GameRoom gameRoom = mock(GameRoom.class);
        String userOneId = "1";
        String userTwoId = "2";
        UserChoices choice1 = new UserChoices(gameRoomId, userOneId, UserChoice.LEAVE);
        UserChoices choice2 = new UserChoices(gameRoomId, userTwoId, UserChoice.LEAVE);

        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(gameRoom);

        gameSessionService.processUserChoices(choice1); // 첫 번째 유저 선택 처리
        Object result = gameSessionService.processUserChoices(choice2); // 두 번째 유저 선택 처리

        assertInstanceOf(GameStatus.class, result);
        assertEquals(GameState.LEAVE, ((GameStatus) result).getGameState());
    }

    @Test
    @DisplayName("게임 종료 후 유저 선택 - 유저 선택이 서로 다를 때 성공 테스트")
    @Disabled
    void testProcessUserChoicesPlayAgainAndLEAVE() {
        Long gameRoomId = 1L;
        GameRoom gameRoom = mock(GameRoom.class);
        String userOneId = "1";
        String userTwoId = "2";
        UserChoices choice1 = new UserChoices(gameRoomId, userOneId, UserChoice.PLAY_AGAIN);
        UserChoices choice2 = new UserChoices(gameRoomId, userTwoId, UserChoice.LEAVE);

        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(gameRoom);

        gameSessionService.processUserChoices(choice1); // 첫 번째 유저 선택 처리
        Object result = gameSessionService.processUserChoices(choice2); // 두 번째 유저 선택 처리

        assertInstanceOf(GameStatus.class, result);
        assertEquals(GameState.ENTER, ((GameStatus) result).getGameState());
    }
}
