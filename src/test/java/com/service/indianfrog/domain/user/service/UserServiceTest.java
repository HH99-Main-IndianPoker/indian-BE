package com.service.indianfrog.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.service.indianfrog.domain.user.dto.UserRequestDto.SignupUserRequestDto;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.RestApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일이 존재하는 회원가입")
    public void signupExistEmail() throws Exception{
        //given
        SignupUserRequestDto requestDto = new SignupUserRequestDto("email@email.com", "password",
            "nickname");
        //when
        when(userRepository.existsByEmail("email@email.com")).thenReturn(true);
        //then
        assertThrows(RestApiException.class, ()->{
            userService.signup(requestDto);
        });
    }

    @Test
    @DisplayName("닉네임이 존재하는 회원가입")
    public void signupExistNickName() throws Exception{
        //given
        SignupUserRequestDto requestDto = new SignupUserRequestDto("email@email.com", "password",
            "nickname");
        //when
        when(userRepository.existsByNickname("nickname")).thenReturn(true);
        //then
        assertThrows(RestApiException.class, ()->{
            userService.signup(requestDto);
        });
    }
}