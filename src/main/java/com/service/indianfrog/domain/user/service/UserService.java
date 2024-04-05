package com.service.indianfrog.domain.user.service;

import com.service.indianfrog.domain.user.dto.UserRequestDto.SignupUserRequestDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto.GetUserResponseDto;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    @Transactional
    public void signup(SignupUserRequestDto requestDto) {
        if (memberRepository.existsByEmail(requestDto.email())) {
            throw new RestApiException(ErrorCode.ALREADY_EXIST_EMAIL.getMsg());
        }

        if (memberRepository.existsByNickname(requestDto.nickname())) {
            throw new RestApiException(ErrorCode.ALREADY_EXIST_NICKNAME.getMsg());
        }

        String password = passwordEncoder.encode(requestDto.password());
        User member = memberRepository.save(requestDto.toEntity(password));
    }

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public GetUserResponseDto getMember(String email) {
        User member = memberRepository.findByEmail(email).orElseThrow(() ->
                new RestApiException(ErrorCode.NOT_FOUND_MEMBER.getMsg()));
        return new GetUserResponseDto(member);
    }

    // 이메일 중복 체크
    public boolean emailCheck(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 닉네임 중복체크
    public boolean nicknameCheck(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
