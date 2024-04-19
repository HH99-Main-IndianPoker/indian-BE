package com.service.indianfrog.gameroom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.QGameRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class GameRoomRepositoryTest {

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private EntityManager em;

    @Test
    public void testFindByRoomId() {
        GameRoom gameRoom = GameRoom.builder()
                .roomName("Test Room")
                .gameState(GameState.READY)
                .build();

        gameRoomRepository.save(gameRoom);

        GameRoom foundGameRoom = gameRoomRepository.findByRoomId(gameRoom.getRoomId());

        assertThat(foundGameRoom).isNotNull();
        assertThat(foundGameRoom.getRoomId()).isEqualTo(gameRoom.getRoomId());
        assertThat(foundGameRoom.getRoomName()).isEqualTo("Test Room");
        assertThat(foundGameRoom.getGameState()).isEqualTo(GameState.READY);
    }

    @Test
    public void testCustomQueryWithQueryDSL() {
        GameRoom gameRoom = GameRoom.builder()
                .roomName("Test Room")
                .gameState(GameState.READY)
                .build();
        gameRoomRepository.save(gameRoom);

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QGameRoom qGameRoom = QGameRoom.gameRoom;
        GameRoom foundGameRoom = queryFactory
                .selectFrom(qGameRoom)
                .where(qGameRoom.roomId.eq(gameRoom.getRoomId()))
                .fetchOne();

        assertThat(foundGameRoom).isNotNull();
        assertThat(foundGameRoom.getRoomId()).isEqualTo(gameRoom.getRoomId());
        assertThat(foundGameRoom.getRoomName()).isEqualTo("Test Room");
        assertThat(foundGameRoom.getGameState()).isEqualTo(GameState.READY);
    }
}
