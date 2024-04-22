package com.service.indianfrog.domain.user.repository;

import com.service.indianfrog.domain.ranking.dto.Ranking.*;
import com.service.indianfrog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    @Query(value = "SELECT infp.user.image_url, DENSE_RANK() OVER (ORDER BY points DESC) AS ranking, nickname, points " +
            "FROM infp.user " +
            "LIMIT 100", nativeQuery = true)
    List<Object[]> findUsersWithRank();


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User findByNickname(String nickname);
//    String findByAuthority(AuthorityType authorityType);
}
