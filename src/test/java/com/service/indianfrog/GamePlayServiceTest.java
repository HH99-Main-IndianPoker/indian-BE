package com.service.indianfrog;

import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.service.GamePlayService;
import com.service.indianfrog.domain.game.service.GameTurnService;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GamePlayServiceTest {

    @Mock
    private GameValidator gameValidator;
    @Mock
    private GameTurnService gameTurnService;

    private GamePlayService gamePlayService;
    private Game game;
    private User user;
    private Turn turn;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gamePlayService = new GamePlayService(gameValidator, gameTurnService);

        game = mock(Game.class);
        user = mock(User.class);
        turn = mock(Turn.class);

        when(gameValidator.validateAndRetrieveGameRoom(anyLong())).thenReturn(mock(GameRoom.class));
        when(gameValidator.initializeOrRetrieveGame(any(GameRoom.class))).thenReturn(game);
        when(gameValidator.findUserByNickname(anyString())).thenReturn(user);
        when(gameTurnService.getTurn(anyLong())).thenReturn(turn);
    }

    @Test
    @DisplayName("플레이어 베팅 - 체크 성공 테스트")
    @Disabled
    void testPlayerActionCheck() {
        when(turn.getCurrentPlayer()).thenReturn(user);
        when(game.getBetAmount()).thenReturn(100);
        when(user.getPoints()).thenReturn(200);

        GameState result = gamePlayService.playerAction(1L, "nickname", "check");

        assertEquals(GameState.ACTION, result);
        verify(user).setPoints(100);
        verify(game).setPot(100);
    }

    @Test
    @DisplayName("플레이어 베팅 - 레이즈 성공 테스트")
    @Disabled
    void testPlayerActionRaise() {
        when(user.getPoints()).thenReturn(200);
        when(game.getBetAmount()).thenReturn(100);
        when(game.getPot()).thenReturn(0);

        GameState result = gamePlayService.playerAction(1L, "nickname", "raise");

        assertEquals(GameState.ACTION, result);
        verify(user).setPoints(0);
        verify(game).setPot(200);
    }

    @Test
    @DisplayName("플레이어 베팅 - 다이 성공 테스트")
    @Disabled
    void testPlayerActionDie() {
        when(game.getPlayerOne()).thenReturn(user);
        when(game.getPlayerTwo()).thenReturn(mock(User.class));
        when(game.getPot()).thenReturn(100);

        GameState result = gamePlayService.playerAction(1L, "nickname", "die");

        assertEquals(GameState.END, result);
        verify(game).setFoldedUser(user);
    }

    @Test
    @DisplayName("플레이어 액션 실패 - 게임 룸 검증 실패")
    @Disabled
    void testPlayerActionWithInvalidGameRoom() {
        Long gameRoomId = 1L;
        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> gamePlayService.playerAction(gameRoomId, "nickname", "check"));
    }

    @Test
    @DisplayName("플레이어 액션 실패 - 턴 정보 가져오기 실패")
    @Disabled
    void testPlayerActionWithInvalidTurnInfo() {
        Long gameRoomId = 1L;
        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(mock(GameRoom.class));
        when(gameValidator.initializeOrRetrieveGame(any(GameRoom.class))).thenReturn(mock(Game.class));
        when(gameValidator.findUserByNickname(anyString())).thenReturn(mock(User.class));
        when(gameTurnService.getTurn(anyLong())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> gamePlayService.playerAction(gameRoomId, "nickname", "raise"));
    }
}
