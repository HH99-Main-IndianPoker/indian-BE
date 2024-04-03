package com.example.socketpractice.domain.user.entity;

import com.example.socketpractice.domain.game.entity.Card;
import com.example.socketpractice.domain.user.entity.type.AuthorityType;
import com.example.socketpractice.global.entity.Timestamped;
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

    private int points = 100;


    @Builder
    public User(Long id, String nickname, String email, String password, AuthorityType authority) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.authority = authority;
    }
}
