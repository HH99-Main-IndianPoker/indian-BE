package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {


    GameRoom findByRoomId(Long gameRoomId);

    GameRoom findByCurrentGame(Game game);
}
