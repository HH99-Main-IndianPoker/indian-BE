package com.service.indianfrog;

import com.service.indianfrog.domain.game.dto.GameDto;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.service.EndGameService;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndGameServiceTest {

    @Mock
    private GameValidator gameValidator;

    @Mock
    private User playerOne;
    @Mock
    private User playerTwo;
    @InjectMocks
    private EndGameService endGameService;

    private Game game;
    private GameRoom gameRoom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        gameRoom = mock(GameRoom.class);
        game = new Game(playerOne, playerTwo);

        Card playerOneCard = Card.DECK1_CARD1;
        Card playerTwoCard = Card.DECK1_CARD2;

        game.setPlayerOneCard(playerOneCard);
        game.setPlayerTwoCard(playerTwoCard);

        // 성공 테스트 용
//        when(gameValidator.validateAndRetrieveGameRoom(anyLong())).thenReturn(gameRoom);
//        when(gameValidator.initializeOrRetrieveGame(any(GameRoom.class))).thenReturn(game);

        // 실패 테스트 용
        when(gameValidator.validateAndRetrieveGameRoom(anyLong())).thenReturn(null);
    }

    @Test
    @DisplayName("라운드 종료 성공 테스트")
    @Disabled
    void endRound() {
        game.incrementRound();

        GameDto.EndRoundResponse response = endGameService.endRound(1L);

        assertNotNull(response);
        assertEquals("START", response.getGameState());
        assertEquals(1, response.getRound());
    }

    @Test
    @DisplayName("게임 종료 성공 테스트")
    @Disabled
    void endGame() {
        game.incrementRound();
        game.incrementRound();
        game.incrementRound();

        GameDto.EndGameResponse response = endGameService.endGame(1L);

        assertNotNull(response);
        assertEquals("USER_CHOICE", response.getGameState());
    }

    @Test
    @DisplayName("라운드 종료 실패 테스트")
    void endRoundFail() {
        assertThrows(Exception.class, () -> endGameService.endRound(1L));
    }

    @Test
    @DisplayName("게임 종료 실패 테스트")
    void endGameFail() {
        assertThrows(Exception.class, () -> endGameService.endGame(1L));
    }
}
