package com.service.indianfrog;

import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.service.GameTurnService;
import com.service.indianfrog.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameTurnServiceTest {

    @Mock
    private User playerOne;

    @Mock
    private User playerTwo;

    private GameTurnService gameTurnService;
    private Long gameId;
    private Turn turn;

    private List<User> players = new ArrayList<>();

    @BeforeEach
    void setUp() {
        gameTurnService = new GameTurnService();
        gameId = 1L;
        players.add(playerOne);
        players.add(playerTwo);
        turn = new Turn(players);
    }

    @Test
    @DisplayName("턴 생성 서비스 성공 코드")
    @Disabled
    void setAndGetTurn() {
        gameTurnService.setTurn(gameId, turn);
        assertNotNull(gameTurnService.getTurn(gameId));

        Turn retrievedTurn = gameTurnService.getTurn(gameId);
        assertEquals(turn, retrievedTurn);
    }
}
