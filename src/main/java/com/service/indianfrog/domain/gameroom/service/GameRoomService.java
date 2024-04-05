package com.service.indianfrog.domain.gameroom.service;

import com.service.indianfrog.domain.gameroom.dto.GameRoomDto;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameRoomService {

    private GameRoomRepository gameRoomRepository;
    private ValidateRoomRepository validateRoomRepository;

    public GameRoomService(GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
    }

    public GameRoomDto createGameRoom(GameRoomDto gameroomdto) {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setRoomName(gameroomdto.getRoomName());
        gameRoom.setCreateAt(new Date());
        gameRoom = gameRoomRepository.save(gameRoom);

        // 나중에 게임방 생성하면 생성자가 자동으로 접속하는 로직.  ,String creatorParticipant
//        gameRoom.getParticipants().add(creatorParticipant);
//        gameRoom = gameRoomRepository.save(gameRoom);
        return convertToDto(gameRoom);
    }

    public void deleteGameRoom(Long roomId) {
        gameRoomRepository.deleteById(roomId);
    }

    public boolean existsById(Long roomId) {
        return gameRoomRepository.existsById(roomId);
    }

    public String filterMessage(String message) {
        if (message == null) {
            return ""; //일단은 null이면 빈 메세지를 반환하게 해놨는데 이게 필요할지 더 고민 필요...
        }
        // 욕설 필터링
        return message.replaceAll("(씨발|병신|ㅅㅂ)", "**");
    }
    @Transactional
    public GameRoomDto addParticipant(Long roomId, String participant) {
        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found!"));

        if (gameRoom.getValidateRooms().size() >= 2) {
            throw new IllegalStateException("The game room is full.");
        }

        ValidateRoom validateRoom = new ValidateRoom();
        validateRoom.setParticipants(participant);
        validateRoom.setGameRoom(gameRoom);

        validateRoomRepository.save(validateRoom);

        return convertToDto(gameRoom);
    }


    @Transactional
    public GameRoomDto removeParticipant(Long roomId, String participant) {
        ValidateRoom validateRoom = validateRoomRepository.findByGameRoomRoomIdAndParticipants(roomId, participant)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found in room!"));
        validateRoomRepository.delete(validateRoom);

        //if (gameRoom.getValidateRooms().isEmpty()) 이걸 썼었는데 지연로딩 문제가 발생... jpa를 이용하는 방식으로 변경해서 지연로딩 방지.
        boolean isEmpty = validateRoomRepository.existsByGameRoomRoomId(roomId);
        if (!isEmpty) {
            gameRoomRepository.deleteById(roomId);
            return null;
        }

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found!"));
        return convertToDto(gameRoom);
    }

    private GameRoomDto convertToDto(GameRoom gameRoom) {
        Set<String> participants = gameRoom.getValidateRooms().stream()
                .map(ValidateRoom::getParticipants)
                .collect(Collectors.toSet());

        return new GameRoomDto(gameRoom.getRoomId(), gameRoom.getRoomName(), participants);
    }

}
