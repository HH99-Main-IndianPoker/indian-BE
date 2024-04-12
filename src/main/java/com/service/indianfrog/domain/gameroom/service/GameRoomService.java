package com.service.indianfrog.domain.gameroom.service;

import com.service.indianfrog.domain.gameroom.dto.GameRoomRequestDto.GameRoomCreateRequestDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GameRoomCreateResponseDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GetGameRoomResponseDto;
import com.service.indianfrog.domain.gameroom.dto.ParticipantInfo;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
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
@Slf4j
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

    /**
     * 주어진 ID에 해당하는 게임방 정보를 조회
     * @param roomId 게임방 ID
     * @return 조회된 게임방의 상세 정보를 담은 Dto
     */
    public GetGameRoomResponseDto getGameRoomById(Long roomId) {
        // roomId르 이용하여 해당 게임방을 조회
        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

        // 해당 게임방에 등록된 모든 ValidateRoom 객체들을 가져와서 그 중에서 방장을 찾음
        String hostName = gameRoom.getValidateRooms().stream()
                .filter(ValidateRoom::isHost)
                // 게임방에는 방장이 1명만 존재하기때문에 findFirst()를 사용해서 방장을 찾음
                .findFirst()
                // 해당 ValidateRoom에 방장 닉네임을 반환
                .map(ValidateRoom::getParticipants)
                .orElse(null); // 바장이 없으면 null을 반환

        // 해당 게임방의 참가자의 수
        int participantCount = gameRoom.getValidateRooms().size();
        return new GetGameRoomResponseDto(gameRoom.getRoomId(), gameRoom.getRoomName(), participantCount, hostName, gameRoom.getGameState());
    }

    /**
     * 모든 게임방을 페이지 단위로 조회
     * @param pageable 페이징 정보
     * @return 페이징 처리된 게임방 목록
     */
    public Page<GetGameRoomResponseDto> getAllGameRooms(Pageable pageable) { //로직 자체는 특정방 조회와 같으나 페이징 처리
        return gameRoomRepository.findAll(pageable)
                // 각각의 게임방 정보를 담기위해 map 사용
                .map(gameRoom -> {
                    // 각 게임방 방장의 닉네임을 찾음
                    String hostName = gameRoom.getValidateRooms().stream()
                            .filter(ValidateRoom::isHost)
                            .findFirst()
                            .map(ValidateRoom::getParticipants)
                            .orElse(null);

                    int participantCount = gameRoom.getValidateRooms().size();
                    return new GetGameRoomResponseDto(gameRoom.getRoomId(), gameRoom.getRoomName(), participantCount, hostName, gameRoom.getGameState());
                });
    }

    /**
     * 주어진 ID를 가진 게임방을 삭제
     * @param roomId 삭제할 게임방 ID
     */
    public void deleteGameRoom(Long roomId) {
        gameRoomRepository.deleteById(roomId);
    }

    /**
     * 주어진 ID의 게임방이 존재하는지 여부를 확인
     * @param roomId 게임방 ID
     * @return 존재 여부
     */
    public boolean existsById(Long roomId) { // 채팅을 사용하기 위해 게임방이 있는지 확인
        return gameRoomRepository.existsById(roomId);
    }

    /**
     * 초기화 시 호출되어 욕설 필터 패턴을 설정
     */
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

    /**
     * 메시지에서 욕설을 필터링
     * @param message 필터링할 메시지
     * @return 필터링된 메시지
     */
    public String filterMessage(String message) {
        return (message == null || message.trim().isEmpty()) ? message : pattern.matcher(message).replaceAll("**");
    }


    /**
     * 새로운 게임방을 생성
     * @param gameRoomDto 게임방 생성 요청 데이터
     * @param principal 현재 인증된 사용자 정보
     * @return 생성된 게임방의 상세 정보
     */
    @Transactional
    public GameRoomCreateResponseDto createGameRoom(GameRoomCreateRequestDto gameRoomDto, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String nickname = user.getNickname();

        LocalDateTime now = LocalDateTime.now();
        GameRoom savedGameRoom = gameRoomRepository.save(gameRoomDto.toEntity());

        ValidateRoom validateRoom = new ValidateRoom().builder()
                .participants(nickname)
                .gameRoom(savedGameRoom)
                // 방 생성한 사람이 최초의 방장이니까 방장으로 설정
                .host(true)
                .build();
        validateRoomRepository.save(validateRoom);
        // 방생성시 최초의 인원은 1명이니까 participantCount를 1로 설정
        return new GameRoomCreateResponseDto(savedGameRoom.getRoomId(), savedGameRoom.getRoomName(), 1, nickname, savedGameRoom.getGameState(), now);
    }

    /**
     * 게임방에 참가자를 추가
     * @param roomId 참가할 게임방 ID
     * @param userDetails 참가자 정보
     * @return 추가된 참가자의 정보
     */
    @Transactional
    public ParticipantInfo addParticipant(Long roomId, UserDetails userDetails) {
        String email = userDetails.getUsername();
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

        ValidateRoom validateRoom = new ValidateRoom().builder()
                .participants(nickname)
                .gameRoom(gameRoom)
                .build();
        validateRoom = validateRoomRepository.save(validateRoom);

        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByGameRoomRoomId(roomId);

        String host = validateRooms.stream().filter(ValidateRoom::isHost).map(ValidateRoom::getParticipants).findFirst().orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String participant = validateRooms.stream().filter(p -> !p.isHost()).map(ValidateRoom::getParticipants).findFirst().orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        int participantPoint = user.getPoints();

        User hostInfo = userRepository.findByNickname(host);

        int hostPoint = hostInfo.getPoints();

        return new ParticipantInfo(participant, host, participantPoint, hostPoint);
    }


    /**
     * 게임방에서 참가자를 제거
     * @param roomId 게임방 ID
     * @param participant 제거할 참가자 정보
     * @return 업데이트된 게임방의 상세 정보
     */
    @Transactional
    public GetGameRoomResponseDto removeParticipant(Long roomId, Principal participant) {
        String email = participant.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String nickname = user.getNickname();

        //특정 게임방에있는 특정 참가자 찾기
        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByGameRoomRoomIdAndParticipants(roomId, nickname);

        // 해당 참가자가 방장인지 확인. 방장이면 true 반환. 순회하는 이유는 한명이 여러 게임방에 참여하고 있을 가능성도 있기 때문.
        boolean wasHost = validateRooms.stream().anyMatch(ValidateRoom::isHost);
        validateRooms.forEach(validateRoomRepository::delete);

        // 방장의 닉네임을 담을 변수 초기화
        String hostName = null;

        //나간게 방장이었다면 새로운 방장 지정
        if (wasHost) {
            List<ValidateRoom> remainingParticipants = validateRoomRepository.findAllByGameRoomRoomId(roomId);
            if (!remainingParticipants.isEmpty()) {
                // 남아있는 첫번째 유저를 방장으로 지정
                ValidateRoom newHost = remainingParticipants.get(0);
                newHost.updateHost();
            }
        } else {
            // wasHost가 아니라면, 기존 방장의 닉네임을 유지
            hostName = validateRoomRepository.findAllByGameRoomRoomId(roomId).stream()
                    .filter(ValidateRoom::isHost)
                    .findFirst()
                    .map(ValidateRoom::getParticipants)
                    .orElse(null);
        }

        // 만약에 방이 비었으면 방을 삭제해야 하니까 방이 비었는지 확인하고 삭제.
        boolean isRoomEmpty = !validateRoomRepository.existsByGameRoomRoomId(roomId);
        if (isRoomEmpty) {
            gameRoomRepository.deleteById(roomId);
            return null;
        }

        // 수정된 방 정보 저장.
        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

        int remainParticipantCount = validateRoomRepository.findAllByGameRoomRoomId(roomId).size();

        return new GetGameRoomResponseDto(gameRoom.getRoomId(), gameRoom.getRoomName(), remainParticipantCount, hostName, gameRoom.getGameState());
    }

    /**
     * 비정상적인 종료시 세션 ID를 통해 게임방에서 참가자를 제거
     * @param sessionId 제거할 참가자의 WebSocket 세션 ID
     */
    @Transactional
    public void removeParticipantBySessionId(String sessionId) {
        // 세션 저장소에서 주어진 세션 ID에 해당하는 사용자의 닉네임을 조회
        String nickname = sessionMappingStorage.getNicknameBySessionId(sessionId);
        if (nickname == null) {
            return;
        }

        // 사용자의 닉네임을 기준으로 모든 게임방 참여 정보를 조회
        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByParticipants(nickname);
        // 검색된 모든 참여 정보를 삭제
        for (ValidateRoom validateRoom : validateRooms) {
            validateRoomRepository.delete(validateRoom);
        }
        // 세션 저장소에서 해당 세션 ID를 제거
        sessionMappingStorage.removeSession(sessionId);
    }

}