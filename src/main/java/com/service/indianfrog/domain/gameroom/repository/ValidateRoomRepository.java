package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ValidateRoomRepository extends JpaRepository<ValidateRoom, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom = :gameRoom AND vr.participants = :participant")
    Optional<ValidateRoom> findByGameRoomAndParticipants(GameRoom gameRoom, String participant);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom gr WHERE gr.roomId = :roomId")
    List<ValidateRoom> findAllByGameRoomRoomId(Long roomId);

    List<ValidateRoom> findAllByReadyTrue();

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom.roomId = :roomId AND vr.participants = :nickname")
    ValidateRoom findByGameRoomRoomIdAndParticipants(Long roomId, String nickname);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom.roomId = :roomId")
    ValidateRoom findByGameRoomRoomId(Long roomId);

    int countByGameRoomRoomId(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom.roomId = :roomId")
    List<ValidateRoom> findAllValidateRoomsByRoomId(Long roomId);

}

