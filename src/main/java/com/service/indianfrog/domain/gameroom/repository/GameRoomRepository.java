package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long>{
    @Query("SELECT gr FROM GameRoom gr LEFT JOIN FETCH gr.validateRooms WHERE gr.roomId = :gameRoomId")
    @Lock(LockModeType.PESSIMISTIC_READ)
    GameRoom findByRoomId(Long gameRoomId);
}
