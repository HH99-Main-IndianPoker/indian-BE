package com.service.indianfrog.domain.game.utils;

import com.service.indianfrog.domain.gameroom.repository.GameRoomRepository;
import com.service.indianfrog.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class RepositoryHolder {
    public final GameRoomRepository gameRoomRepository;
    public final UserRepository userRepository;

    public RepositoryHolder(GameRoomRepository gameRoomRepository, UserRepository userRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.userRepository = userRepository;
    }
}
