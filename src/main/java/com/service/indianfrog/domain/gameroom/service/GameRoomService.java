package com.service.indianfrog.domain.gameroom.service;

import com.service.indianfrog.domain.gameroom.dto.GameRoomDto;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    public GameRoomDto getGameRoomById(Long roomId) {
        Optional<GameRoom> optionalGameRoom = gameRoomRepository.findById(roomId);
        return optionalGameRoom.map(this::convertToDto).orElse(null);
    }

    public List<GameRoomDto> getAllGameRooms() {
        List<GameRoom> gameRooms = gameRoomRepository.findAll();
        return gameRooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByGameRoomRoomIdAndParticipants(roomId, participant);
        if (validateRooms.isEmpty()) {
            throw new IllegalArgumentException("Participant not found in room!");
        }

        validateRooms.forEach(validateRoomRepository::delete);

        boolean isRoomEmpty = !validateRoomRepository.existsByGameRoomRoomId(roomId);
        if (isRoomEmpty) {
            gameRoomRepository.deleteById(roomId);
            return null; // 방이 비었으니 삭제하고 null 반환
        }

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found!"));
        return convertToDto(gameRoom); // 수정된 게임방 정보 반환
    }




    private GameRoomDto convertToDto(GameRoom gameRoom) {
        Set<String> participants = gameRoom.getValidateRooms().stream()
                .map(ValidateRoom::getParticipants)
                .collect(Collectors.toSet());

        return new GameRoomDto(gameRoom.getRoomId(), gameRoom.getRoomName(), participants);
    }

}
