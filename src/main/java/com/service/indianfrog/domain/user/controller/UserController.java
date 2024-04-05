package com.service.indianfrog.domain.user.controller;

import com.service.indianfrog.domain.user.controller.docs.UserControllerDocs;
import com.service.indianfrog.domain.user.dto.UserRequestDto;
import com.service.indianfrog.domain.user.service.UserService;
import com.service.indianfrog.domain.user.valid.UserValidationSequence;
import com.service.indianfrog.global.dto.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
@Validated(UserValidationSequence.class)
public class UserController implements UserControllerDocs {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseDto signup(@RequestBody @Valid UserRequestDto.SignupUserRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseDto.success("회원가입 성공", null);
    }

}
