package com.service.indianfrog;

import static org.junit.jupiter.api.Assertions.*;

import com.service.indianfrog.domain.game.entity.Turn;
import com.service.indianfrog.domain.game.service.GameTurnService;
import com.service.indianfrog.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StartGameServiceTest {

    private GameTurnService gameTurnService;

    @BeforeEach
    void setUp() {
        gameTurnService = new GameTurnService();
    }

    @Test
    void testSetAndGetTurn() {
        // Given
        Long gameId = 1L;
        User playerOne = new User();
        User playerTwo = new User();
        List<User> players = List.of(playerOne, playerTwo);
        Turn expectedTurn = new Turn(players);

        // When
        gameTurnService.setTurn(gameId, expectedTurn);
        Turn retrievedTurn = gameTurnService.getTurn(gameId);

        // Then
        assertNotNull(retrievedTurn, "The turn should not be null.");
        assertEquals(expectedTurn, retrievedTurn, "The retrieved turn should match the expected turn.");
    }

    @Test
    void testTurnNotPresent() {
        // Given
        Long gameId = 2L;

        // When
        Turn retrievedTurn = gameTurnService.getTurn(gameId);

        // Then
        assertNull(retrievedTurn, "The turn should be null when not set.");
    }
}