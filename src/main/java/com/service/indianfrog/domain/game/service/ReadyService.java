package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
public class ReadyService {

    private final GameRoomRepository gameRoomRepository;
    private final ValidateRoomRepository validateRoomRepository;
    private final UserRepository userRepository;
    private final MeterRegistry registry;
    private final Timer totalGameReadyTimer;

    public ReadyService(GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository,
                        UserRepository userRepository, MeterRegistry registry) {
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
        this.userRepository = userRepository;
        this.registry = registry;
        this.totalGameReadyTimer = registry.timer("totalReady.time");
    }

    @Transactional
    public GameStatus gameReady(Long gameRoomId, Principal principal) {
        return totalGameReadyTimer.record(() -> {
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_USER.getMessage()));
            GameRoom gameRoom = gameRoomRepository.findById(gameRoomId).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));
            ValidateRoom validateRoom = validateRoomRepository.findByGameRoomAndParticipants(gameRoom, user.getNickname()).orElseThrow(() -> new RestApiException(ErrorCode.GAME_ROOM_NOW_FULL.getMessage()));

            validateRoom.revert(validateRoom.isReady());

            Timer.Sample getValidateRoomTimer = Timer.start(registry);
            List<ValidateRoom> validateRooms = validateRoomRepository.findAllByReadyTrue();
            getValidateRoomTimer.stop(registry.timer("readyValidate.time"));

            if (validateRooms.size() == 2) {
                return new GameStatus(gameRoomId, user.getNickname(), GameState.ALL_READY);
            }

            if (validateRooms.size() == 1 && validateRoom.isReady() == true) {
                return new GameStatus(gameRoomId, user.getNickname(), GameState.READY);
            }

            if (validateRooms.size() == 1 && validateRoom.isReady() == false) {
                return new GameStatus(gameRoomId, user.getNickname(), GameState.UNREADY);
            }

            return new GameStatus(gameRoomId, user.getNickname(), GameState.NO_ONE_READY);
        });
    }
}
