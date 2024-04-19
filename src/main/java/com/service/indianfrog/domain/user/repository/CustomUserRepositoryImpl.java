package com.service.indianfrog.domain.user.repository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.indianfrog.domain.user.entity.User;

import java.util.Optional;

import static com.service.indianfrog.domain.user.entity.QUser.user;

public class CustomUserRepositoryImpl implements CustomUserRepository{

    private final JPAQueryFactory jpaQueryFactory;

    public CustomUserRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(user)
                        .where(user.email.eq(email))
                        .fetchOne()
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaQueryFactory
                .selectOne()
                .from(user)
                .where(user.email.eq(email))
                .fetchFirst() != null;
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaQueryFactory
                .selectOne()
                .from(user)
                .where(user.nickname.eq(nickname))
                .fetchFirst() != null;
    }

    @Override
    public User findByNickname(String nickname) {
        return jpaQueryFactory
                .selectFrom(user)
                .where(user.nickname.eq(nickname))
                .fetchOne();
    }
}
