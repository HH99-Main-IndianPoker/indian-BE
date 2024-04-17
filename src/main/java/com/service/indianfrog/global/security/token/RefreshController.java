package com.service.indianfrog.global.security.token;

import com.service.indianfrog.global.dto.ResponseDto;
import com.service.indianfrog.global.dto.TokenRequest;
import com.service.indianfrog.global.dto.TokenResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@Slf4j
@Controller
public class RefreshController {

    private final RefreshTokenService tokenService;

    public RefreshController(RefreshTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @DeleteMapping("token/logout")
    public ResponseDto logout(@RequestHeader("Authorization") final String accessToken) {

        // 엑세스 토큰으로 현재 Redis 정보 삭제
        tokenService.removeRefreshToken(accessToken);
        return ResponseDto.success("로그아웃 성공", null);
    }

    /*
     * 로테이트시 기존 리프레시토큰 못쓰게 막아야함.*/
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponseStatus> refresh(@RequestHeader("Authorization") String accessToken,
        HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        String newAccessToken = tokenService.republishAccessTokenWithRotate(accessToken,request, response);
        return ResponseEntity.ok(TokenResponseStatus.addStatus(200, newAccessToken));
    }
}
