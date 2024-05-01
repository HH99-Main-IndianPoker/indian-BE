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

    private int points;

    /*
     * rank
     */
    private int wins;
    private int losses;

    private String imageUrl;
    private String originFileName;

    @Builder
    public User(Long id, String nickname, String email, String password, String imageUrl, String originFileName) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.points = 100;
        this.imageUrl = imageUrl;
        this.originFileName = originFileName;
    }

    public User() {

    }

    public void imgUpdate(String originFileName, String imageUrl) {
        this.originFileName = originFileName;
        this.imageUrl = imageUrl;
    }

    public void incrementWins() {
        this.wins++;
    }

    public void incrementLosses() {
        this.losses++;
    }

    public void updatePoint(int point) {
        this.points = this.points + point;
    }

    public void decreasePoints(int point) {
        this.points = Math.max(this.points - point, 0);
    }

    public void takePoint(int point) {
        this.points = this.points - point;
    }

    public void updatePassword(String resetPassword) {
        this.password = resetPassword;
    }
}
