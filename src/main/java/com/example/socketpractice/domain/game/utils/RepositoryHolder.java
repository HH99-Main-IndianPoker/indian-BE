package com.example.socketpractice.domain.game.utils;

import com.example.socketpractice.domain.game.repository.GameRoomRepository;
import com.example.socketpractice.domain.user.repository.UserRepository;
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
