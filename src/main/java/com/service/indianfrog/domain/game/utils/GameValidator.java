package com.service.indianfrog.domain.game.utils;

import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class GameValidator {
    /* 생성자를 통한 필드 주입 */
    private final RepositoryHolder repositoryHolder;

    public GameValidator(RepositoryHolder repositoryHolder) {
        this.repositoryHolder = repositoryHolder;
    }

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_READ)
    public synchronized GameRoom validateAndRetrieveGameRoom(Long gameRoomId) {
        return repositoryHolder.gameRoomRepository.findByRoomId(gameRoomId);
    }

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public synchronized void gameValidate(GameRoom gameRoom) {

        log.info("game is null : {}", repositoryHolder.gameRepository.existsByGameRoom(gameRoom));

            List<ValidateRoom> validateRooms = repositoryHolder.validateRoomRepository.findAllByGameRoomRoomId(gameRoom.getRoomId());

            String host = validateRooms.stream()
                    .filter(ValidateRoom::isHost)
                    .map(ValidateRoom::getParticipants)
                    .findFirst()
                    .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
            String participant = validateRooms.stream()
                    .filter(p -> !p.isHost())
                    .map(ValidateRoom::getParticipants)
                    .findFirst()
                    .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

            User playerOne = repositoryHolder.userRepository.findByNickname(host);
            User playerTwo = repositoryHolder.userRepository.findByNickname(participant);

            if (gameRoom.getCurrentGame() == null) {
                Game game = Game.builder().playerOne(playerOne).playerTwo(playerTwo).gameRoom(gameRoom).build();
                repositoryHolder.gameRepository.save(game);
                gameRoom.startNewGame(game);
            }

            if (gameRoom.getCurrentGame() != null) {
                gameRoom.getCurrentGame().startGame(playerOne, playerTwo);
            }

    }

public User findUserByNickname(String nickname) {
    return repositoryHolder.userRepository.findByNickname(nickname);
//                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
}
}
