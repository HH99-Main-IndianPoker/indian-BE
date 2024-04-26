package com.service.indianfrog.domain.game.utils;

import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameValidator {
    /* 생성자를 통한 필드 주입 */
    private final RepositoryHolder repositoryHolder;

    public GameValidator(RepositoryHolder repositoryHolder) {
        this.repositoryHolder = repositoryHolder;
    }

    public GameRoom validateAndRetrieveGameRoom(Long gameRoomId) {
        return repositoryHolder.gameRoomRepository.findByRoomId(gameRoomId);
    }

    @Transactional
    public Game initializeOrRetrieveGame(GameRoom gameRoom) {
        Game game = gameRoom.getCurrentGame();

        if (game == null) {
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
            gameRoom.startNewGame(playerOne, playerTwo);
            repositoryHolder.gameRoomRepository.save(gameRoom);
            game = gameRoom.getCurrentGame();
        }

        return game;
    }

    public User findUserByNickname(String nickname) {
        return repositoryHolder.userRepository.findByNickname(nickname);
//                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
    }
}
