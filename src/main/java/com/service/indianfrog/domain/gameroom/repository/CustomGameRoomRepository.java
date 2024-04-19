package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.gameroom.entity.GameRoom;

public interface CustomGameRoomRepository {

    GameRoom findByRoomId(Long gameRoomId);
}