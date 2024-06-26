//package com.service.indianfrog.domain.gameroom.util;
//
//import com.service.indianfrog.domain.gameroom.entity.GameRoom;
//import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
//import com.service.indianfrog.domain.gameroom.dto.GameRoomResponseDto.GameRoomCreateResponseDto;
//import com.service.indianfrog.domain.game.entity.GameState;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@SpringBootApplication
//public class Dummy {
//
//    public static void main(String[] args) {
//        SpringApplication.run(Dummy.class, args);
//    }
//
//    @Bean
//    @Transactional
//    public CommandLineRunner loadData(GameRoomRepository gameRoomRepository) {
//        return (args) -> {
//            List<GameRoom> gameRooms = IntStream.rangeClosed(1, 100000)
//                    .mapToObj(i -> GameRoom.builder()
//                            .roomName("Room " + i)
//                            .validateRooms(new HashSet<>()) // 빈 validateRooms 세트로 설정
//                            .currentGame(null) // 초기에는 currentGame을 null로 설정
//                            .gameState(GameState.READY) // 초기 상태를 READY로 설정
//                            .build())
//                    .collect(Collectors.toList());
//
//            gameRoomRepository.saveAll(gameRooms);
//
//            // DTO로 변환
//            List<GameRoomCreateResponseDto> gameRoomCreateResponseDtos = gameRooms.stream()
//                    .map(gameRoom -> new GameRoomCreateResponseDto(
//                            gameRoom.getRoomId(),
//                            gameRoom.getRoomName(),
//                            gameRoom.getValidateRooms().size(),
//                            "HostName", // 호스트 이름 예시
//                            0, // myPoint 예시
//                            gameRoom.getGameState(),
//                            "HostImgUrl", // 호스트 이미지 URL 예시
//                            LocalDateTime.now() // 현재 시간 설정
//                    ))
//                    .collect(Collectors.toList());
//
//            // 예시로 DTO를 출력하거나 반환할 수 있습니다.
//            gameRoomCreateResponseDtos.forEach(System.out::println);
//        };
//    }
//}
