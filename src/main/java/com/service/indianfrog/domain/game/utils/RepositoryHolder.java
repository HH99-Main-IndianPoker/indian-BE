package com.service.indianfrog.domain.game.utils;

import com.service.indianfrog.domain.game.repository.GameRepository;
import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.gameroom.repository.ValidateRoomRepository;
import com.service.indianfrog.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class RepositoryHolder {
    public final GameRoomRepository gameRoomRepository;
    public final UserRepository userRepository;
    public final ValidateRoomRepository validateRoomRepository;
    public final GameRepository gameRepository;
    public RepositoryHolder(GameRoomRepository gameRoomRepository, UserRepository userRepository, ValidateRoomRepository validateRoomRepository, GameRepository gameRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.userRepository = userRepository;
        this.validateRoomRepository = validateRoomRepository;
        this.gameRepository = gameRepository;
    }
}
