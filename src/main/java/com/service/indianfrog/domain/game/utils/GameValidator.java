package com.service.indianfrog.domain.game.utils;

import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameRoom;
import com.service.indianfrog.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GameValidator {
    /* 생성자를 통한 필드 주입 */
    private final RepositoryHolder repositoryHolder;

    public GameValidator(RepositoryHolder repositoryHolder) {
        this.repositoryHolder = repositoryHolder;
    }

    public GameRoom validateAndRetrieveGameRoom(Long gameRoomId) {
        return repositoryHolder.gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게임방 입니다."));

    }

    public Game initializeOrRetrieveGame(GameRoom gameRoom) {
        Game game = gameRoom.getCurrentGame();
        if (game == null) {
            gameRoom.startNewGame(gameRoom.getPlayerOne(), gameRoom.getPlayerTwo());
            repositoryHolder.gameRoomRepository.save(gameRoom);
            game = gameRoom.getCurrentGame();
        }
        return game;
    }

    /* 게임 룸 정보 업데이트 로직 팀원과 의논을 통해 수정 필요*/
    public void saveGameRoomState(GameRoom gameRoom) {
        repositoryHolder.gameRoomRepository.save(gameRoom);
    }

    public User findUserByNickname(String nickname) {
        return repositoryHolder.userRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
    }
}
