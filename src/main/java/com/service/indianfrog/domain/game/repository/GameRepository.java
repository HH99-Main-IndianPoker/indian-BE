package com.service.indianfrog.domain.game.repository;

import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByGameRoom(GameRoom gameRoom);

    boolean existsByGameRoom(GameRoom gameRoom);
}
