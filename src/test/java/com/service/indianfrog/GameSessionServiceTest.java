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
        GameRoom gameRoom = new GameRoom();
        String userOneId = "1";
        String userTwoId = "2";
        UserChoices choices = new UserChoices(gameRoomId, userOneId, userTwoId, UserChoice.PLAY_AGAIN, UserChoice.PLAY_AGAIN);

        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(gameRoom);

        List<GameStatus> statusList = gameSessionService.processUserChoices(choices);

        assertEquals(2, statusList.size());
        assertEquals(GameState.START, statusList.get(0).getGameState());
        assertEquals(GameState.START, statusList.get(1).getGameState());
    }

    @Test
    @DisplayName("게임 종료 후 유저 선택 - 게임 나가기 성공 테스트")
    @Disabled
    void testProcessUserChoicesBothLeave() {
        Long gameRoomId = 1L;
        GameRoom gameRoom = new GameRoom();
        String userOneId = "1";
        String userTwoId = "2";
        UserChoices choices = new UserChoices(gameRoomId, userOneId, userTwoId, UserChoice.LEAVE, UserChoice.LEAVE);

        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(gameRoom); // 로직에 따라 적절한 모의 객체 반환

        List<GameStatus> statusList = gameSessionService.processUserChoices(choices);

        assertEquals(2, statusList.size());
        assertEquals(GameState.LEAVE, statusList.get(0).getGameState());
        assertEquals(GameState.LEAVE, statusList.get(1).getGameState());
    }

    @Test
    @DisplayName("게임 종료 후 유저 선택 - 유저 선택이 서로 다를 때 성공 테스트")
    @Disabled
    void testProcessUserChoicesPlayAgainAndLEAVE() {
        Long gameRoomId = 1L;
        GameRoom gameRoom = new GameRoom();
        String userOneId = "1";
        String userTwoId = "2";
        UserChoices choices = new UserChoices(gameRoomId, userOneId, userTwoId, UserChoice.PLAY_AGAIN, UserChoice.LEAVE);

        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(gameRoom); // 로직에 따라 적절한 모의 객체 반환

        List<GameStatus> statusList = gameSessionService.processUserChoices(choices);

        assertEquals(2, statusList.size());
        assertEquals(GameState.ENTER, statusList.get(0).getGameState());
        assertEquals(GameState.LEAVE, statusList.get(1).getGameState());
    }

    @Test
    @DisplayName("게임 룸 검증 실패 시 예외 발생 테스트")
    @Disabled
    void testProcessUserChoicesWithException() {
        Long gameRoomId = 1L;
        when(gameValidator.validateAndRetrieveGameRoom(anyLong())).thenThrow(new RuntimeException("Game room validation failed"));

        Exception exception = assertThrows(RuntimeException.class, () -> gameSessionService.processUserChoices(new UserChoices(gameRoomId, "1", "2", UserChoice.PLAY_AGAIN, UserChoice.LEAVE)));

        assertNotNull(exception);
        assertEquals("Game room validation failed", exception.getMessage());
    }
}
