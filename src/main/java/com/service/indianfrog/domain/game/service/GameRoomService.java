package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameRoomDto;
import com.service.indianfrog.domain.game.entity.GameRoom;
import com.service.indianfrog.domain.game.repository.GameRoomRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class GameRoomService {

    private GameRoomRepository gameRoomRepository;
    public GameRoomService(GameRoomRepository gameRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
    }


    public GameRoom createGameRoom(GameRoomDto gameRoomDto) {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setRoomName(gameRoomDto.getRoomName());
        gameRoom.setCreateAt(new Date());

        return gameRoomRepository.save(gameRoom);
    }

    public void deleteGameRoom(Long roomId) {
        gameRoomRepository.deleteById(roomId);
    }

    public boolean existsById(Long roomId) {
        return gameRoomRepository.existsById(roomId);
    }

    public String filterMessage(String message) {
        // 욕설 필터링
        return message.replaceAll("(씨발|병신|ㅅㅂ)", "**");
    }
}
