package com.service.indianfrog.domain.user.controller;

import com.service.indianfrog.domain.user.controller.docs.UserControllerDocs;
import com.service.indianfrog.domain.user.dto.UserRequestDto.SignupUserRequestDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto.EmailSendResponseDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto.GetUserResponseDto;
import com.service.indianfrog.domain.user.dto.UserResponseDto.SignupResponseDto;
import com.service.indianfrog.domain.user.service.EmailService;
import com.service.indianfrog.domain.user.service.UserService;
import com.service.indianfrog.domain.user.valid.UserValidationGroup;
import com.service.indianfrog.domain.user.valid.UserValidationSequence;
import com.service.indianfrog.global.dto.ResponseDto;
import com.service.indianfrog.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.service.indianfrog.domain.user.valid.UserValidationGroup.*;


@RestController
//@RequestMapping("/user")
@Validated(UserValidationSequence.class)
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final EmailService emailService;

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    // 회원가입
    @PostMapping("/user/signup")
    public ResponseDto<SignupResponseDto> signup(
        @RequestBody @Valid SignupUserRequestDto requestDto) {
        SignupResponseDto responseDto = userService.signup(requestDto);
        return ResponseDto.success("회원가입 성공", responseDto);
    }

    @GetMapping("/user/email/check")
    public ResponseEntity emailCheck(
        @RequestParam
        @NotBlank(message = "이메일을 입력해주세요.", groups = UserValidationGroup.class)
        @Email(message = "잘못된 이메일 형식입니다.", groups = EmailGroup.class)
        String email) {
        boolean validEmail = userService.emailCheck(email);
        return ResponseEntity.ok(ResponseDto.success("사용 가능한 이메일입니다.", validEmail));
    }

    @Valid
    @GetMapping("/user/nickname/check")
    public ResponseEntity nicknameCheck(
        @RequestParam
        @NotBlank(message = "닉네임을 입력해주세요.", groups = NotBlankGroup.class)
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리를 입력 해주세요.", groups = NicknamePatternGroup.class)
        String nickname
    ) {
        boolean validNickName = userService.nicknameCheck(nickname);
        return ResponseEntity.ok(ResponseDto.success("사용 가능한 닉네임입니다.", validNickName));
    }

    /*My-page*/
    @GetMapping("/member/mypage")
    public ResponseDto<GetUserResponseDto> getUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GetUserResponseDto responseDto = userService.getMember(userDetails.getUsername());
        return ResponseDto.success("회원 정보 조회 성공", responseDto);
    }

    /*email duplicate*/
    @PostMapping("/user/email-code")
    public ResponseDto<EmailSendResponseDto> emailSend(
        @RequestParam
        @NotBlank(message = "이메일을 입력해주세요.", groups = NotBlankGroup.class)
        @Email(message = "잘못된 이메일 형식입니다.", groups = EmailGroup.class)
        String email
    ) {
        String emailCode = emailService.emailSend(email);
        return ResponseDto.success("이메일 인증 코드 발송 성공", new EmailSendResponseDto(emailCode));
    }

}
