package com.service.indianfrog.domain.game.service;

import com.service.indianfrog.domain.game.dto.GameStatus;
import com.service.indianfrog.domain.game.entity.Card;
import com.service.indianfrog.domain.game.entity.Game;
import com.service.indianfrog.domain.game.entity.GameState;
import com.service.indianfrog.domain.game.utils.GameValidator;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;

import static com.service.indianfrog.global.exception.ErrorCode.INSUFFICIENT_POINTS;

@Service
public class ReadyService {

    private final GameRoomRepository gameRoomRepository;
    private final ValidateRoomRepository validateRoomRepository;
    private final UserRepository userRepository;
    private final MeterRegistry registry;
    private final Timer totalGameReadyTimer;
    private final GameValidator gameValidator;

    public ReadyService(GameRoomRepository gameRoomRepository, ValidateRoomRepository validateRoomRepository,
                        UserRepository userRepository, MeterRegistry registry, GameValidator gameValidator) {
        this.gameRoomRepository = gameRoomRepository;
        this.validateRoomRepository = validateRoomRepository;
        this.userRepository = userRepository;
        this.registry = registry;
        this.totalGameReadyTimer = registry.timer("totalReady.time");
        this.gameValidator = gameValidator;
    }

    @Transactional
    public GameStatus gameReady(Long gameRoomId, Principal principal) {
        return totalGameReadyTimer.record(() -> {
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_USER.getMessage()));
            GameRoom gameRoom = gameRoomRepository.findById(gameRoomId).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_GAME_ROOM.getMessage()));
            ValidateRoom validateRoom = validateRoomRepository.findByGameRoomAndParticipants(gameRoom, user.getNickname()).orElseThrow(() -> new RestApiException(ErrorCode.GAME_ROOM_NOW_FULL.getMessage()));

            if (!checkReadyPoints(user)) {
                throw new RestApiException(INSUFFICIENT_POINTS.getMessage());
            }

            validateRoom.revert(validateRoom.isReady());

            Timer.Sample getValidateRoomTimer = Timer.start(registry);
            List<ValidateRoom> validateRooms = validateRoomRepository.findAllByGameRoomAndReadyTrue(gameRoom);
            getValidateRoomTimer.stop(registry.timer("readyValidate.time"));

            if (validateRooms.size() == 2) {
                gameValidator.gameValidate(gameRoom);
                firstCardShuffle(gameRoom.getCurrentGame());
                return new GameStatus(gameRoomId, user.getNickname(), GameState.ALL_READY);
            }

            if (validateRooms.size() == 1 && validateRoom.isReady() == true) {
                return new GameStatus(gameRoomId, user.getNickname(), GameState.READY);
            }

            if (validateRooms.size() == 1 && validateRoom.isReady() == false) {
                return new GameStatus(gameRoomId, user.getNickname(), GameState.UNREADY);
            }

            return new GameStatus(gameRoomId, user.getNickname(), GameState.NO_ONE_READY);
        });
    }

    private boolean checkReadyPoints(User user) {
        return user.getPoints() > 0;
    }

    private void firstCardShuffle(Game game) {
        /* 카드를 섞은 후 플레이어에게 각각 한장 씩 제공
         * 플레이어에게 제공한 카드는 사용한 카드목록에 포함되어 다음 라운드에서는 사용되지 않는다*/
        List<Card> availableCards = prepareAvailableCards(game);
        Collections.shuffle(availableCards);

        Card playerOneCard = availableCards.get(0);
        Card playerTwoCard = availableCards.get(1);

        game.setPlayerOneCard(playerOneCard);
        game.setPlayerTwoCard(playerTwoCard);

        game.addUsedCard(playerOneCard);
        game.addUsedCard(playerTwoCard);
    }

    private List<Card> prepareAvailableCards(Game game) {
        /* 사용한 카드 목록과 전체 카드 목록을 가져옴
         * 전체 카드 목록에서 사용한 카드 목록을 제외하고 남은 카드 목록을 반환한다*/
        Set<Card> usedCards = game.getUsedCards();
        Set<Card> allCards = EnumSet.allOf(Card.class); // 성능 개선 여지 있음
        allCards.removeAll(usedCards);
        return new ArrayList<>(allCards);
    }
}
