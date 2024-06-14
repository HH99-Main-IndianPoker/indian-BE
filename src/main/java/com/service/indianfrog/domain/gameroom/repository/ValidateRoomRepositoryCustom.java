package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;

import java.util.List;
import java.util.Optional;

public interface ValidateRoomRepositoryCustom {

    Optional<ValidateRoom> findByGameRoomAndParticipants(GameRoom gameRoom, String participant);

    List<ValidateRoom> findAllByGameRoomRoomId(Long roomId);

    ValidateRoom findByGameRoomRoomIdAndParticipants(Long roomId, String nickname);

    ValidateRoom findByGameRoomRoomId(Long roomId);

    List<ValidateRoom> findAllValidateRoomsByRoomId(Long roomId);

    List<ValidateRoom> findAllByGameRoomAndReadyTrue(GameRoom gameRoom);

    int countByGameRoomRoomId(Long roomId);
}
