package com.service.indianfrog.domain.gameroom.service;

import com.service.indianfrog.domain.gameroom.dto.GameRoomRequestDto.GameRoomCreateRequestDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GameRoomCreateResponseDto;
import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GetAllGameRoomResponseDto;
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
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import io.micrometer.core.instrument.Timer;

@Service
@Slf4j
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final ValidateRoomRepository validateRoomRepository;
    private final UserRepository userRepository;
    private Pattern pattern;
    private SessionMappingStorage sessionMappingStorage;
    private final Timer getGameRoomTimer;
    private final Timer getValidateRoomsTimer;
    private final Timer getAllGameRoomTimer;
    private final MeterRegistry registry;

    public GameRoomService(GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository, UserRepository userRepository,
                           SessionMappingStorage sessionMappingStorage, MeterRegistry registry) {
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
        this.userRepository = userRepository;
        this.sessionMappingStorage = sessionMappingStorage;
        this.getGameRoomTimer = registry.timer("getGameRoomById.time");
        this.getValidateRoomsTimer = registry.timer("getParticipant.time");
        this.getAllGameRoomTimer = registry.timer("getAllGameRoom.time");
        this.registry = registry;
    }

    /**
     * 주어진 ID에 해당하는 게임방 정보를 조회
     *
     * @param roomId 게임방 ID
     * @return 조회된 게임방의 상세 정보를 담은 Dto
     */
    @Transactional(readOnly = true)
    public GetGameRoomResponseDto getGameRoomById(Long roomId) {
        // roomId를 이용하여 해당 게임방을 조회
        return getGameRoomTimer.record(() -> {
            GameRoom gameRoom = gameRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

            // 해당 게임방의 모든 유효성 검증방 정보를 한 번에 불러옴
            List<ValidateRoom> validateRooms = getValidateRoomsTimer.record(() ->
                    validateRoomRepository.findAllValidateRoomsByRoomId(roomId));

            // 방장 정보 추출
            ValidateRoom host = validateRooms.stream()
                    .filter(ValidateRoom::isHost)
                    .findFirst()
                    .orElse(null);

            // 초기화 안해주니까 에러남.
            String hostNickname = null;
            int hostPoints = 0;
            String hostImageUrl = null;

            if (host != null) {
                hostNickname = host.getParticipants();
                hostPoints = userRepository.findByNickname(hostNickname).getPoints();
                hostImageUrl = userRepository.findByNickname(hostNickname).getImageUrl();
            }

            String participantNickname = null;
            int participantPoints = 0;
            String participantImageUrl = null;

            // 다른 참가자 찾기
            ValidateRoom participant = validateRooms.stream()
                    .filter(v -> !v.isHost()) // 방장 제외
                    .findFirst()
                    .orElse(null);

            if (participant != null) {
                participantNickname = participant.getParticipants();
                participantPoints = userRepository.findByNickname(participantNickname).getPoints();
                participantImageUrl = userRepository.findByNickname(participantNickname).getImageUrl();
            }

            // 게임방 정보와 참가자 정보 반환
            return new GetGameRoomResponseDto(gameRoom.getRoomId(), gameRoom.getRoomName(), gameRoom.getGameState(), validateRooms.size(),
                    hostNickname, hostPoints, hostImageUrl, participantNickname, participantPoints, participantImageUrl);
        });
    }


    /**
     * 모든 게임방을 페이지 단위로 조회
     *
     * @return 페이징 처리된 게임방 목록
     */
    @Transactional(readOnly = true)
    public Page<GetAllGameRoomResponseDto> getAllGameRooms() {

        Pageable pageable = PageRequest.of(0, 15, Sort.by("created_at").descending());

        return getAllGameRoomTimer.record(() -> gameRoomRepository.findAll(pageable)
                // 각각의 게임방 정보를 담기위해 map 사용
                .map(gameRoom -> {
                    // 각 게임방 방장의 닉네임을 찾음
                    String hostName = gameRoom.getValidateRooms().stream()
                            .filter(ValidateRoom::isHost)
                            .findFirst()
                            .map(ValidateRoom::getParticipants)
                            .orElse(null);

                    int participantCount = gameRoom.getValidateRooms().size();
                    return new GetAllGameRoomResponseDto(gameRoom.getRoomId(), gameRoom.getRoomName(), participantCount, hostName, gameRoom.getGameState());
                }));
    }


    /**
     * 주어진 ID를 가진 게임방을 삭제
     *
     * @param roomId 삭제할 게임방 ID
     */
    @Transactional
    public void deleteGameRoom(Long roomId) {
        Timer.Sample deleteRoomTimer = Timer.start(registry);
        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));

        validateRoomRepository.deleteAll(gameRoom.getValidateRooms());
        validateRoomRepository.flush();

        gameRoomRepository.delete(gameRoom);
        gameRoomRepository.flush();
        deleteRoomTimer.stop(registry.timer("deleteGameRoom.time"));
    }


    /**
     * 주어진 ID의 게임방이 존재하는지 여부를 확인
     *
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
     *
     * @param message 필터링할 메시지
     * @return 필터링된 메시지
     */
    public String filterMessage(String message) {
        return (message == null || message.trim().isEmpty()) ? message : pattern.matcher(message).replaceAll("**");
    }


    /**
     * 새로운 게임방을 생성
     *
     * @param gameRoomDto 게임방 생성 요청 데이터
     * @param principal   현재 인증된 사용자 정보
     * @return 생성된 게임방의 상세 정보
     */
    @Transactional
    public GameRoomCreateResponseDto createGameRoom(GameRoomCreateRequestDto gameRoomDto, Principal principal) {
        Timer.Sample createRoomTimer = Timer.start(registry);

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        LocalDateTime now = LocalDateTime.now();
        GameRoom savedGameRoom = gameRoomRepository.save(gameRoomDto.toEntity());

        ValidateRoom validateRoom = new ValidateRoom().builder()
                .participants(user.getNickname())
                .gameRoom(savedGameRoom)
                // 방 생성한 사람이 최초의 방장이니까 방장으로 설정
                .host(true)
                .build();
        validateRoomRepository.save(validateRoom);
        // 방생성시 최초의 인원은 1명이니까 participantCount를 1로 설정
        createRoomTimer.stop(registry.timer("createGameRoom.time"));
        return new GameRoomCreateResponseDto(savedGameRoom.getRoomId(), savedGameRoom.getRoomName(), 1, user.getNickname(), user.getPoints(), savedGameRoom.getGameState(), user.getImageUrl(),now);
    }

    /**
     * 게임방에 참가자를 추가
     *
     * @param roomId      참가할 게임방 ID
     * @param userDetails 참가자 정보
     * @return 추가된 참가자의 정보
     */
    @Transactional
    public ParticipantInfo addParticipant(Long roomId, UserDetails userDetails) {
        Timer.Sample addParticipantTimer = Timer.start(registry);

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

        String host = validateRooms.stream()
                .filter(ValidateRoom::isHost)
                .map(ValidateRoom::getParticipants)
                .findFirst()
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_HOST.getMessage()));

        String participant = validateRooms.stream()
                .filter(p -> !p.isHost())
                .map(ValidateRoom::getParticipants)
                .findFirst()
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));

        int participantPoint = user.getPoints();

        User hostInfo = userRepository.findByNickname(host);

        int hostPoint = hostInfo.getPoints();

        addParticipantTimer.stop(registry.timer("addParticipant.time"));
        return new ParticipantInfo(participant, host, participantPoint, hostPoint, user.getImageUrl(), hostInfo.getImageUrl());
    }


    /**
     * 게임방에서 참가자를 제거
     *
     * @param roomId      게임방 ID
     * @param participant 제거할 참가자 정보
     */
    @Transactional
    public void removeParticipant(Long roomId, Principal participant) {
        Timer.Sample removeParticipantTimer = Timer.start(registry);

        String email = participant.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String nickname = user.getNickname();

        //특정 게임방에있는 특정 참가자 찾기
        ValidateRoom validateRoom = validateRoomRepository.findByGameRoomRoomIdAndParticipants(roomId, nickname);

        if (validateRoomRepository.countByGameRoomRoomId(roomId) == 1) {
            validateRoomRepository.delete(validateRoom);
            gameRoomRepository.deleteById(roomId);
        }

        if (validateRoomRepository.countByGameRoomRoomId(roomId) == 2) {
            validateRoomRepository.delete(validateRoom);
            ValidateRoom newHost = validateRoomRepository.findByGameRoomRoomId(roomId);
            newHost.updateHost();
        }
        removeParticipantTimer.stop(registry.timer("removeParticipant.time"));
    }

    /**
     * 비정상적인 종료시 세션 ID를 통해 게임방에서 참가자를 제거
     *
     * @param sessionId 제거할 참가자의 WebSocket 세션 ID
     */
    @Transactional
    public void removeParticipantBySessionId(String sessionId) {
        Timer.Sample removeSessionTimer = Timer.start(registry);
        // 세션 저장소에서 주어진 세션 ID에 해당하는 사용자의 닉네임을 조회
        String nickname = sessionMappingStorage.getNicknameBySessionId(sessionId);
        if (nickname == null) {
            return;
        }

        // 사용자의 닉네임을 기준으로 모든 게임방 참여 정보를 조회
        List<ValidateRoom> validateRooms = validateRoomRepository.findAllByParticipants(nickname);
        // 검색된 모든 참여 정보를 삭제
        validateRoomRepository.deleteAll(validateRooms);
        // 세션 저장소에서 해당 세션 ID를 제거
        sessionMappingStorage.removeSession(sessionId);
        removeSessionTimer.stop(registry.timer("removeSession.time"));
    }
}