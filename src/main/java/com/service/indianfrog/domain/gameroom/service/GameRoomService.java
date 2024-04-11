package com.service.indianfrog.domain.gameroom.service;

import com.service.indianfrog.domain.gameroom.dto.GameRoomRequestDto.GameRoomCreateRequestDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GameRoomCreateResponseDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GetGameRoomResponseDto;
import com.service.indianfrog.domain.gameroom.dto.ValidateRoomDto;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import com.service.indianfrog.domain.gameroom.util.SessionMappingStorage;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final ValidateRoomRepository validateRoomRepository;
    private final UserRepository userRepository;
    private Pattern pattern;
    private SessionMappingStorage sessionMappingStorage;

    public GameRoomService(GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository, UserRepository userRepository, SessionMappingStorage sessionMappingStorage) {
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
        this.userRepository = userRepository;
        this.sessionMappingStorage = sessionMappingStorage;
    }

    public GetGameRoomResponseDto getGameRoomById(Long roomId) {
        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

        String hostNickname = gameRoom.getValidateRooms().stream()
                .filter(ValidateRoom::isHost)
                .findFirst()
                .map(ValidateRoom::getParticipants)
                .orElse(null);

        int participantCount = gameRoom.getValidateRooms().size();
        return new GetGameRoomResponseDto(gameRoom.getRoomId(), gameRoom.getRoomName(), participantCount, hostNickname);
    }

    public Page<GetGameRoomResponseDto> getAllGameRooms(Pageable pageable) {
        return gameRoomRepository.findAll(pageable)
                .map(gameRoom -> {
                    // 각 게임방의 방장 닉네임 찾기
                    String hostNickname = gameRoom.getValidateRooms().stream()
                            .filter(ValidateRoom::isHost)
                            .findFirst()
                            .map(ValidateRoom::getParticipants)
                            .orElse(null);

                    int participantCount = gameRoom.getValidateRooms().size();

                    return new GetGameRoomResponseDto(gameRoom.getRoomId(),gameRoom.getRoomName(),participantCount,hostNickname);
                });
    }

    public void deleteGameRoom(Long roomId) {
        gameRoomRepository.deleteById(roomId);
    }

    public boolean existsById(Long roomId) {
        return gameRoomRepository.existsById(roomId);
    }

    @PostConstruct
    public void init() {
        try {
            pattern = Pattern.compile(
                    Files.lines(Paths.get(new ClassPathResource("bad-words.txt").getURI()))
                            .map(Pattern::quote)
                            .collect(Collectors.joining("|")),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            );
        } catch (IOException e) {
            pattern = Pattern.compile(""); // 비어있는 패턴
        }
    }
    public String filterMessage(String message) {
        return (message == null || message.trim().isEmpty()) ? message : pattern.matcher(message).replaceAll("**");
    }

    @Transactional
    public GameRoomCreateResponseDto createGameRoom(GameRoomCreateRequestDto gameRoomDto, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String nickname = user.getNickname();

        LocalDateTime now = LocalDateTime.now();
        GameRoom savedGameRoom = gameRoomRepository.save(gameRoomDto.toEntity());

        ValidateRoom validateRoom = new ValidateRoom();
        validateRoom.setParticipants(nickname);
        validateRoom.setGameRoom(savedGameRoom);
        validateRoom.setHost(true);
        validateRoomRepository.save(validateRoom);

        return new GameRoomCreateResponseDto(savedGameRoom.getRoomId(), savedGameRoom.getRoomName(), 1, nickname, now);
    }

    @Transactional
    public ValidateRoomDto addParticipant(Long roomId, Principal participant) {
        String email = participant.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String nickname = user.getNickname();

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

        if (gameRoom.getValidateRooms().size() >= 2) {
            throw new RestApiException(ErrorCode.GAME_ROOM_NOW_FULL.getMessage());
        }

        if (validateRoomRepository.findByGameRoomAndParticipants(gameRoom, nickname).isPresent()) {
            throw new RestApiException(ErrorCode.ALREADY_EXIST_USER.getMessage());
        }

        ValidateRoom validateRoom = new ValidateRoom();
        validateRoom.setParticipants(nickname);
        validateRoom.setGameRoom(gameRoom);
        validateRoom = validateRoomRepository.save(validateRoom);

        return new ValidateRoomDto(validateRoom.getValidId(), validateRoom.getParticipants(), validateRoom.isHost());
    }

    @Transactional
    public GetGameRoomResponseDto removeParticipant(Long roomId, Principal participant) {
        String email = participant.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String nickname = user.getNickname();

        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByGameRoomRoomIdAndParticipants(roomId, nickname);
        if (validateRooms.isEmpty()) {
            throw new RestApiException(ErrorCode.GAME_USER_HAS_GONE.getMessage());
        }

        boolean wasHost = validateRooms.stream().anyMatch(ValidateRoom::isHost);
        validateRooms.forEach(validateRoomRepository::delete);

        String hostNickname = null; // 방장의 닉네임을 담을 변수 초기화

        if (wasHost) {
            List<ValidateRoom> remainingParticipants = validateRoomRepository.findAllByGameRoomRoomId(roomId);
            if (!remainingParticipants.isEmpty()) {
                ValidateRoom newHost = remainingParticipants.get(0);
                newHost.setHost(true);
                validateRoomRepository.save(newHost);
                hostNickname = newHost.getParticipants(); // 새 방장의 닉네임 설정
            }
        } else {
            // wasHost가 아니라면, 기존 방장의 닉네임을 유지
            hostNickname = validateRoomRepository.findAllByGameRoomRoomId(roomId).stream()
                    .filter(ValidateRoom::isHost)
                    .findFirst()
                    .map(ValidateRoom::getParticipants)
                    .orElse(null);
        }

        boolean isRoomEmpty = !validateRoomRepository.existsByGameRoomRoomId(roomId);
        if (isRoomEmpty) {
            gameRoomRepository.deleteById(roomId);
            return null;
        }

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

        int remainingParticipantCount = validateRoomRepository.findAllByGameRoomRoomId(roomId).size();

        return new GetGameRoomResponseDto(gameRoom.getRoomId(), gameRoom.getRoomName(), remainingParticipantCount, hostNickname);
    }



    @Transactional
    public void removeParticipantBySessionId(String sessionId) {
        String nickname = sessionMappingStorage.getNicknameBySessionId(sessionId);
        if (nickname == null) {
            return;
        }

        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByParticipants(nickname);
        for (ValidateRoom validateRoom : validateRooms) {
            validateRoomRepository.delete(validateRoom);
        }

        sessionMappingStorage.removeSession(sessionId);
    }

}