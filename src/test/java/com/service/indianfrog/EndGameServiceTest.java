package com.service.indianfrog;

import com.service.indianfrog.domain.game.dto.GameDto;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameRoom;
import com.service.indianfrog.domain.game.service.EndGameService;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

        gameRoom = new GameRoom();
        game = new Game(playerOne, playerTwo);

        Card playerOneCard = Card.DECK1_CARD1;
        Card playerTwoCard = Card.DECK1_CARD2;

        game.setPlayerOneCard(playerOneCard);
        game.setPlayerTwoCard(playerTwoCard);

        when(gameValidator.validateAndRetrieveGameRoom(anyLong())).thenReturn(gameRoom);
        when(gameValidator.initializeOrRetrieveGame(any(GameRoom.class))).thenReturn(game);
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
}
