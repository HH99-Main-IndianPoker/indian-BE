package com.service.indianfrog.querydslTest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepositoryCustomImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CustomValidateRoomRepositoryTest {

    @Autowired
    private ValidateRoomRepository validateRoomRepository;

    @Autowired
    private EntityManager em;

    private ValidateRoomRepositoryCustomImpl customRepository;
    private JPAQueryFactory queryFactory;

    private GameRoom gameRoom;
    private ValidateRoom validateRoom;

    @BeforeEach
    public void setup() {
        queryFactory = new JPAQueryFactory(em);
        customRepository = new ValidateRoomRepositoryCustomImpl(queryFactory);

        gameRoom = GameRoom.builder()
                .roomName("test")
                .gameState(com.service.indianfrog.domain.game.entity.GameState.READY)
                .build();
        em.persist(gameRoom);

        validateRoom = ValidateRoom.builder()
                .participants("test@test.com")
                .host(false)
                .ready(true)
                .gameRoom(gameRoom)
                .build();
        em.persist(validateRoom);
        em.flush();
        em.clear();
    }

    @Test
    public void testFindByGameRoomAndParticipants() {
        Optional<ValidateRoom> found = customRepository.findByGameRoomAndParticipants(gameRoom, "test@test.com");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getParticipants()).isEqualTo("test@test.com");
    }

    @Test
    public void testFindAllByGameRoomRoomId() {
        List<ValidateRoom> results = customRepository.findAllByGameRoomRoomId(gameRoom.getRoomId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGameRoom().getRoomId()).isEqualTo(gameRoom.getRoomId());
    }

    @Test
    public void testFindByGameRoomRoomIdAndParticipants() {
        ValidateRoom found = customRepository.findByGameRoomRoomIdAndParticipants(gameRoom.getRoomId(), "test@test.com");
        assertThat(found).isNotNull();
        assertThat(found.getParticipants()).isEqualTo("test@test.com");
    }

    @Test
    public void testFindByGameRoomRoomId() {
        ValidateRoom found = customRepository.findByGameRoomRoomId(gameRoom.getRoomId());
        assertThat(found).isNotNull();
        assertThat(found.getGameRoom().getRoomId()).isEqualTo(gameRoom.getRoomId());
    }

    @Test
    public void testFindAllValidateRoomsByRoomId() {
        List<ValidateRoom> results = customRepository.findAllValidateRoomsByRoomId(gameRoom.getRoomId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGameRoom().getRoomId()).isEqualTo(gameRoom.getRoomId());
    }

    @Test
    public void testFindAllByGameRoomAndReadyTrue() {
        List<ValidateRoom> results = customRepository.findAllByGameRoomAndReadyTrue(gameRoom);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).isReady()).isTrue();
    }

    @Test
    public void testCountByGameRoomRoomId() {
        int count = customRepository.countByGameRoomRoomId(gameRoom.getRoomId());
        assertThat(count).isEqualTo(1);
    }
}
