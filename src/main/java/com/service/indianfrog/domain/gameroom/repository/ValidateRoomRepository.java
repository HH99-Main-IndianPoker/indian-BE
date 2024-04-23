package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ValidateRoomRepository extends JpaRepository<ValidateRoom, Long> {

    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom = :gameRoom AND vr.participants = :participant")
    Optional<ValidateRoom> findByGameRoomAndParticipants(GameRoom gameRoom, String participant);

    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom gr WHERE gr.roomId = :roomId")
    List<ValidateRoom> findAllByGameRoomRoomId(Long roomId);

    List<ValidateRoom> findAllByReadyTrue();

    List<ValidateRoom> findAllByParticipants(String email);

    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom.roomId = :roomId AND vr.participants = :nickname")
    ValidateRoom findByGameRoomRoomIdAndParticipants(Long roomId, String nickname);

    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom.roomId = :roomId")
    ValidateRoom findByGameRoomRoomId(Long roomId);

    int countByGameRoomRoomId(Long roomId);

    @Query("SELECT vr FROM ValidateRoom vr JOIN FETCH vr.gameRoom WHERE vr.gameRoom.roomId = :roomId")
    List<ValidateRoom> findAllValidateRoomsByRoomId(Long roomId);

}

