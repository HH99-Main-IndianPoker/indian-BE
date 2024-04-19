package com.service.indianfrog.domain.user.repository;

import com.service.indianfrog.domain.user.entity.User;

import java.util.Optional;

public interface CustomUserRepository {

    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);


    User findByNickname(String nickname);
}
