package com.service.indianfrog.domain.gameroom.service;

import com.service.indianfrog.domain.gameroom.dto.GameRoomDto;
import com.service.indianfrog.domain.gameroom.dto.ValidateRoomDto;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final ValidateRoomRepository validateRoomRepository;

    public GameRoomService(GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
    }

    public GameRoomDto getGameRoomById(Long roomId) {
        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found!"));
        return new GameRoomDto(gameRoom.getRoomId(), gameRoom.getRoomName());
    }

    public Page<GameRoomDto> getAllGameRooms(Pageable pageable) {
        return gameRoomRepository.findAll(pageable).map(gameRoom -> new GameRoomDto(gameRoom.getRoomId(), gameRoom.getRoomName()));
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
    public GameRoomDto createGameRoom(GameRoomDto gameRoomDto, String email) {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setRoomName(gameRoomDto.getRoomName());
        gameRoom.setCreateAt(new Date());
        gameRoom = gameRoomRepository.save(gameRoom);

        ValidateRoom validateRoom = new ValidateRoom();
        validateRoom.setParticipants(email);
        validateRoom.setGameRoom(gameRoom);
        validateRoomRepository.save(validateRoom);

        return new GameRoomDto(gameRoom.getRoomId(), gameRoom.getRoomName());
    }

    @Transactional
    public ValidateRoomDto addParticipant(Long roomId, String participant) {
        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found!"));

        if (validateRoomRepository.findByGameRoomAndParticipants(gameRoom, participant).isPresent()) {
            throw new IllegalStateException("Participant has already joined the room.");
        }

        ValidateRoom validateRoom = new ValidateRoom();
        validateRoom.setParticipants(participant);
        validateRoom.setGameRoom(gameRoom);
        validateRoom = validateRoomRepository.save(validateRoom);

        return new ValidateRoomDto(validateRoom.getValidId(), validateRoom.getParticipants());
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
            // 방이 비었으므로 삭제하고 null 반환
            gameRoomRepository.deleteById(roomId);
            return null;
        }

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found!"));

        // 직접적으로 GameRoomDto를 생성하여 반환
        return new GameRoomDto(gameRoom.getRoomId(), gameRoom.getRoomName());
    }



}
