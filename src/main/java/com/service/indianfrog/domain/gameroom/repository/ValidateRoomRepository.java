package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValidateRoomRepository extends JpaRepository<ValidateRoom, Long> {
    boolean existsByGameRoomRoomId(Long roomId);

    List<ValidateRoom> findAllByGameRoomRoomIdAndParticipants(Long roomId, String participant);

    Optional<ValidateRoom> findByGameRoomAndParticipants(GameRoom gameRoom, String participant);

    List<ValidateRoom> findAllByGameRoomRoomId(Long roomId);

}
