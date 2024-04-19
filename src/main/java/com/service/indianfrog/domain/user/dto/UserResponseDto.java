package com.service.indianfrog.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.service.indianfrog.domain.user.entity.User;

import java.time.LocalDateTime;

public class UserResponseDto {

    public record CheckUserEmailResponseDto(Boolean isExist) {
    }

    public record CheckUserNicknameResponseDto(Boolean isExist) {
    }

    public record SignupResponseDto(
            String email,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt) {
    }

    public record GetUserResponseDto(
            Long id,
            String email,
            String nickname,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,
            Integer points
    ) {
        public GetUserResponseDto(User user) {
            this(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getCreatedAt(),
                    user.getPoints()
            );
        }
    }

    public record EmailSendResponseDto(String emailCode) {
    }

    public record EmailAuthResponseDto(Boolean success) {
    }
}
