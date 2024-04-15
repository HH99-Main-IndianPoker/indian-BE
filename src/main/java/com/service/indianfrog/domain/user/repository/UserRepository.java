package com.service.indianfrog.domain.user.repository;

import com.service.indianfrog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);


    User findByNickname(String nickname);

//    String findByAuthority(AuthorityType authorityType);
}
