package com.service.indianfrog.domain.gameroom.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import static com.service.indianfrog.domain.gameroom.entity.QGameRoom.gameRoom;

public class CustomGameRoomRepositoryImpl implements CustomGameRoomRepository{

    private final JPAQueryFactory queryFactory;

    public CustomGameRoomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public GameRoom findByRoomId(Long gameRoomId) {
        return queryFactory
                .selectFrom(gameRoom)
                .where(gameRoom.roomId.eq(gameRoomId))
                .fetchOne();
    }
}