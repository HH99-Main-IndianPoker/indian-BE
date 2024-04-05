package com.service.indianfrog.domain.game.repository;

import com.service.indianfrog.domain.game.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
}
