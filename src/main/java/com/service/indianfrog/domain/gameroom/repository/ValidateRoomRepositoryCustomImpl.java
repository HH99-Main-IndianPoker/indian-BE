package com.service.indianfrog.domain.gameroom.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.indianfrog.domain.gameroom.entity.GameRoom;
import com.service.indianfrog.domain.gameroom.entity.QGameRoom;
import com.service.indianfrog.domain.gameroom.entity.QValidateRoom;
import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class ValidateRoomRepositoryCustomImpl implements ValidateRoomRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Autowired
    public ValidateRoomRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ValidateRoom> findByGameRoomAndParticipants(GameRoom gameRoom, String participant) {
        QValidateRoom validateRoom = QValidateRoom.validateRoom;
        QGameRoom qGameRoom = QGameRoom.gameRoom;

        ValidateRoom result = queryFactory.selectFrom(validateRoom)
                .join(validateRoom.gameRoom, qGameRoom).fetchJoin()
                .where(validateRoom.gameRoom.eq(gameRoom)
                        .and(validateRoom.participants.eq(participant)))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<ValidateRoom> findAllByGameRoomRoomId(Long roomId) {
        QValidateRoom validateRoom = QValidateRoom.validateRoom;
        QGameRoom qGameRoom = QGameRoom.gameRoom;

        return queryFactory.selectFrom(validateRoom)
                .join(validateRoom.gameRoom, qGameRoom).fetchJoin()
                .where(validateRoom.gameRoom.roomId.eq(roomId))
                .fetch();
    }

    @Override
    public ValidateRoom findByGameRoomRoomIdAndParticipants(Long roomId, String nickname) {
        QValidateRoom validateRoom = QValidateRoom.validateRoom;
        QGameRoom qGameRoom = QGameRoom.gameRoom;

        return queryFactory.selectFrom(validateRoom)
                .join(validateRoom.gameRoom, qGameRoom).fetchJoin()
                .where(validateRoom.gameRoom.roomId.eq(roomId)
                        .and(validateRoom.participants.eq(nickname)))
                .fetchOne();
    }

    @Override
    public ValidateRoom findByGameRoomRoomId(Long roomId) {
        QValidateRoom validateRoom = QValidateRoom.validateRoom;
        QGameRoom qGameRoom = QGameRoom.gameRoom;

        return queryFactory.selectFrom(validateRoom)
                .join(validateRoom.gameRoom, qGameRoom).fetchJoin()
                .where(validateRoom.gameRoom.roomId.eq(roomId))
                .fetchOne();
    }

    @Override
    public List<ValidateRoom> findAllValidateRoomsByRoomId(Long roomId) {
        QValidateRoom validateRoom = QValidateRoom.validateRoom;
        QGameRoom qGameRoom = QGameRoom.gameRoom;

        return queryFactory.selectFrom(validateRoom)
                .join(validateRoom.gameRoom, qGameRoom).fetchJoin()
                .where(validateRoom.gameRoom.roomId.eq(roomId))
                .fetch();
    }

    @Override
    public List<ValidateRoom> findAllByGameRoomAndReadyTrue(GameRoom gameRoom) {
        QValidateRoom validateRoom = QValidateRoom.validateRoom;
        QGameRoom qGameRoom = QGameRoom.gameRoom;

        return queryFactory.selectFrom(validateRoom)
                .join(validateRoom.gameRoom, qGameRoom).fetchJoin()
                .where(validateRoom.gameRoom.eq(gameRoom)
                        .and(validateRoom.ready.isTrue()))
                .fetch();
    }

    @Override
    public int countByGameRoomRoomId(Long roomId) {
        QValidateRoom validateRoom = QValidateRoom.validateRoom;

        return (int) queryFactory.selectFrom(validateRoom)
                .where(validateRoom.gameRoom.roomId.eq(roomId))
                .fetchCount();
    }
}