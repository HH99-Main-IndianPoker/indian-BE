package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReadyService {

    private final GameRoomRepository gameRoomRepository;
    private final ValidateRoomRepository validateRoomRepository;

    public ReadyService(GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
    }

    @Transactional
    public GameStatus gameReady(GameStatus gameStatus, Long gameRoomId) {

        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

        ValidateRoom validateRoom = validateRoomRepository.findByGameRoomAndParticipants(gameRoom, gameStatus.getUserId()).orElseThrow(() -> new RestApiException(ErrorCode.GAME_ROOM_NOW_FULL.getMessage()));

        validateRoom.revert(validateRoom.isReady());

        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByReadyTrue();

        if (validateRooms.size() == 2) {
            return new GameStatus(gameRoomId, gameStatus.getUserId(), GameState.ALL_READY);
        }

        if (validateRooms.size() == 1 && validateRoom.isReady() == true) {
            return new GameStatus(gameRoomId, gameStatus.getUserId(), GameState.READY);
        }

        if (validateRooms.size() == 1 && validateRoom.isReady() == false) {
            return new GameStatus(gameRoomId, gameStatus.getUserId(), GameState.UNREADY);
        }

        return new GameStatus(gameRoomId, gameStatus.getUserId(), GameState.NO_ONE_READY);
    }

}
