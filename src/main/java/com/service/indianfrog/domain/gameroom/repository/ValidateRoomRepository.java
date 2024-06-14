package com.service.indianfrog.domain.gameroom.repository;

import com.service.indianfrog.domain.gameroom.entity.ValidateRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ValidateRoomRepository extends JpaRepository<ValidateRoom, Long>, QuerydslPredicateExecutor<ValidateRoom>, ValidateRoomRepositoryCustom {
}
