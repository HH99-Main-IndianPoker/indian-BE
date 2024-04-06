package com.service.indianfrog;

import com.service.indianfrog.domain.game.dto.GameDto;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameRoom;
import com.service.indianfrog.domain.game.service.GameTurnService;
import com.service.indianfrog.domain.game.service.StartGameService;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.user.entity.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class StartGameServiceTest {

    @Mock
    private GameValidator gameValidator;

    @Mock
    private GameTurnService gameTurnService;

    @Mock
    private User playerOne;

    @Mock
    private User playerTwo;

    @InjectMocks
    private StartGameService startGameService;

    @Test
    @Disabled
    @DisplayName("라운드 시작 성공 테스트")
    public void testStartRound() {
        // Given
        Long gameRoomId = 1L;
        GameRoom gameRoom = new GameRoom();
        Game game = new Game(playerOne, playerTwo);

        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(gameRoom);
        when(gameValidator.initializeOrRetrieveGame(gameRoom)).thenReturn(game);

        // When
        GameDto.StartRoundResponse response = startGameService.startRound(gameRoomId);

        // Then
        verify(gameValidator, times(1)).validateAndRetrieveGameRoom(gameRoomId);
        verify(gameValidator, times(1)).initializeOrRetrieveGame(gameRoom);
        verify(gameValidator, times(1)).saveGameRoomState(gameRoom);

        assertNotNull(response);
        assertEquals("ACTION", response.getGameState());
    }
}
