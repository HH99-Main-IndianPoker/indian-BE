package com.service.indianfrog.domain.user.entity;

import com.service.indianfrog.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

//    @Column(name = "authority", nullable = false)
//    @Enumerated(EnumType.STRING)
//    private AuthorityType authority;

    private int points;

    /*
     * rank
     */
    private int wins;
    private int losses;

    @Builder
    public User(Long id, String nickname, String email, String password) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.points = 100;
//        this.authority = authority;
    }

    public User(String nickname, String encodedPassword, String email, Long socialId) {
        super();
    }

    public User(String nickname, String email, String socialId) {
        super();
    }

    public User() {

    }

    public User kakaoIdUpdate(Long socialId) {
        return this;
    }

    public void updateInfo(String email, String oauthId) {
        this.email = email;
    }

    public void setPoints(int point) {
        this.points = point;
    }

    public void incrementWins() {
        this.wins++;
    }

    public void incrementLosses() {
        this.losses++;
    }

    public void updatePoint(int point) {
        this.points += point;
    }
}
