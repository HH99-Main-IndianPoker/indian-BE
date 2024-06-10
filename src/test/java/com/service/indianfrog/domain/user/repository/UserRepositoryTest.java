package com.service.indianfrog.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.service.indianfrog.domain.user.dto.UserRequestDto;
import com.service.indianfrog.domain.user.dto.UserRequestDto.SignupUserRequestDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto;
import com.service.indianfrog.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("이메일로 유저 찾기")
    public void findUserByEmail() throws Exception{
        //given
        UserRequestDto.SignupUserRequestDto requestDto = new SignupUserRequestDto("email", "password",
            "nickname");
        User user1 = requestDto.toEntity("password");
        userRepository.save(user1);

        //when
        String email = userRepository
            .findByEmail(user1.getEmail())
            .map(User::getEmail)
            .orElse(null);
        //then
        assertThat(email).isEqualTo("email");

    }
}