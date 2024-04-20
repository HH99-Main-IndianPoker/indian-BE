package com.service.indianfrog.domain.gameroom.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;

import java.util.List;
import java.util.Optional;

import static com.service.indianfrog.domain.gameroom.entity.QValidateRoom.validateRoom;

public class CustomValidateRoomRepositoryImpl implements CustomValidateRoomRepository {

    private final JPAQueryFactory queryFactory;

    public CustomValidateRoomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ValidateRoom> findByGameRoomAndParticipants(GameRoom gameRoom, String participant) {
        return Optional.ofNullable(
                queryFactory.selectFrom(validateRoom)
                        .where(validateRoom.gameRoom.eq(gameRoom)
                                .and(validateRoom.participants.eq(participant)))
                        .fetchOne()
        );
    }

    @Override
    public List<ValidateRoom> findAllByGameRoomRoomId(Long roomId) {
        return queryFactory.selectFrom(validateRoom)
                .where(validateRoom.gameRoom.roomId.eq(roomId))
                .fetch();
    }

    @Override
    public List<ValidateRoom> findAllByReadyTrue() {
        return queryFactory.selectFrom(validateRoom)
                .where(validateRoom.ready.isTrue())
                .fetch();
    }

    @Override
    public List<ValidateRoom> findAllByParticipants(String email) {
        return queryFactory.selectFrom(validateRoom)
                .where(validateRoom.participants.eq(email))
                .fetch();
    }

    @Override
    public ValidateRoom findByGameRoomRoomIdAndParticipants(Long roomId, String nickname) {
        return queryFactory.selectFrom(validateRoom)
                .where(validateRoom.gameRoom.roomId.eq(roomId)
                        .and(validateRoom.participants.eq(nickname)))
                .fetchOne();
    }

    @Override
    public ValidateRoom findByGameRoomRoomId(Long roomId) {
        return queryFactory.selectFrom(validateRoom)
                .where(validateRoom.gameRoom.roomId.eq(roomId))
                .fetchOne();
    }

    @Override
    public int countByGameRoomRoomId(Long roomId) {
        long count = queryFactory
                .select(validateRoom.count())
                .from(validateRoom)
                .where(validateRoom.gameRoom.roomId.eq(roomId))
                .fetchOne();

        return (int) count;
    }

}