package com.service.indianfrog.domain.user.service;

import com.service.indianfrog.domain.user.dto.UserRequestDto.PassChangeDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto.ChangedPassDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto.PasswordFindDto;
import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@Slf4j
public class PasswordService {

    private final UserRepository userRepository;
    private final MailSendService mailSendService;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(UserRepository userRepository, MailSendService mailSendService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailSendService = mailSendService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PasswordFindDto findPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
        String subject = "인디안 개구리: 임시 비밀번호 입니다.";
        String resetPassword = makeRandomPw(10);
        mailSendService.sendEmail(email, subject, resetPassword);
        user.updatePassword(passwordEncoder.encode(resetPassword));
        return new UserResponseDto.PasswordFindDto(true);
    }

    @Transactional
    public ChangedPassDto changePassword(User user, PassChangeDto passChangeDto) {

        if(passwordEncoder.matches(passChangeDto.originPassword(), user.getPassword()) && !passChangeDto.updatedPassword().equals(passChangeDto.originPassword())) {
            User updateUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
            updateUser.updatePassword(passwordEncoder.encode(passChangeDto.updatedPassword()));
            log.info(passChangeDto.updatedPassword());
            return new ChangedPassDto(true);
        }
        return new ChangedPassDto(false);
    }

    public String makeRandomPw(int len) {

        SecureRandom rm = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < len; i++) {
            //소문자, 대문자, 숫자
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
            int index = rm.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
