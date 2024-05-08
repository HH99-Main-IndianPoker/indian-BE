package com.service.indianfrog.global.security.token;

import com.service.indianfrog.global.dto.ResponseDto;
import com.service.indianfrog.global.dto.TokenResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@Controller
public class RefreshController {

    private final RefreshTokenService tokenService;

    public RefreshController(RefreshTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /*
     * 로테이트시 기존 리프레시토큰 못쓰게 막아야함.*/
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponseStatus> refresh(
        @RequestHeader("Authorization") String accessToken, HttpServletRequest request,
        HttpServletResponse response) throws UnsupportedEncodingException {
        tokenService.republishAccessTokenWithRotate(accessToken, request, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
