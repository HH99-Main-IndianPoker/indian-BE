package com.service.indianfrog.domain.user.entity;

import com.service.indianfrog.domain.user.entity.type.AuthorityType;
import com.service.indianfrog.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "authority", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthorityType authority;
    private Long kakaoId;


    @Builder
    public User(Long id, String nickname, String email, String password, AuthorityType authority) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.authority = authority;
    }

    public User(String nickname, String encodedPassword, String email, Long kakaoId) {
        super();
    }

    public User kakaoIdUpdate(Long kakaoId) {
        this.kakaoId = kakaoId;
        return this;
    }
}
