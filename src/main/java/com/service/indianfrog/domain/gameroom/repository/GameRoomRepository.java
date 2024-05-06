package com.service.indianfrog.domain.gameroom.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long>{
    @Query("SELECT gr FROM GameRoom gr LEFT JOIN FETCH gr.validateRooms WHERE gr.roomId = :gameRoomId")
    GameRoom findByRoomId(Long gameRoomId);

    boolean existsByCurrentGame(Game game);
}
