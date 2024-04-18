package com.service.indianfrog.domain.game.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.service.indianfrog.domain.game.dto.GameDto.StartRoundResponse;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.service.GameTurnService;
import com.service.indianfrog.domain.game.service.StartGameService;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.user.entity.User;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerTest {

    @Mock
    private GameValidator gameValidator;
    @Mock
    private GameTurnService gameTurnService;
    @Mock
    private GameRoomRepository gameRoomRepository;

    @InjectMocks
    private StartGameService startGameService;

    @Test
    void testStartRound() {
        // Given
        Long gameRoomId = 1L;
        GameRoom mockGameRoom = new GameRoom();
        User playerOne = User.builder()
            .id(1L)
            .nickname("PlayerOne")
            .email("playerone@example.com")
            .password("password1")
            .build();
        User playerTwo = User.builder()
            .id(2L)
            .nickname("PlayerTwo")
            .email("playertwo@example.com")
            .password("password2")
            .build();
        List<User> players = Arrays.asList(playerOne, playerTwo);
        Game mockGame = new Game(playerOne, playerTwo);
        // 카드 랜덤 할당을 가정하고 고정된 값으로 설정
        mockGame.setPlayerOneCard(Card.DECK1_CARD5);
        mockGame.setPlayerTwoCard(Card.DECK2_CARD5);
        Turn mockTurn = new Turn(players);

        when(gameRoomRepository.findByRoomId(gameRoomId)).thenReturn(mockGameRoom);
        when(gameValidator.validateAndRetrieveGameRoom(gameRoomId)).thenReturn(mockGameRoom);
        when(gameValidator.initializeOrRetrieveGame(mockGameRoom)).thenReturn(mockGame);
        when(gameTurnService.getTurn(mockGame.getId())).thenReturn(mockTurn);

        // When
        StartRoundResponse response = startGameService.startRound(gameRoomId, principal);

        // Then
        assertNotNull(response);
        assertEquals("ACTION", response.getGameState());
        assertEquals(1, response.getRound());
        assertNotNull(response.getPlayerOneInfo());
        assertNotNull(response.getPlayerTwoInfo());
        // 카드 할당 확인

        assertNotNull(response.getTurn());
        assertEquals(playerOne, response.getTurn().getCurrentPlayer());

        // Verify interaction
        verify(gameRoomRepository).findByRoomId(gameRoomId);
        verify(gameValidator).saveGameRoomState(mockGameRoom);
        verify(gameTurnService).getTurn(mockGame.getId());
    }
}