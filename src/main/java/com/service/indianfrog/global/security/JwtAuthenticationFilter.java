package com.service.indianfrog.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.indianfrog.domain.user.dto.UserRequestDto.LoginRequestDto;
import com.service.indianfrog.global.exception.LoginException;
import com.service.indianfrog.global.jwt.JwtUtil;
import com.service.indianfrog.global.security.dto.GeneratedToken;
import com.service.indianfrog.global.security.filter.CustomResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/user/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            if (requestDto.email() == null | requestDto.email().isEmpty()) {
                throw new LoginException("이메일을 입력해 주세요.");
            } else if (requestDto.password() == null | requestDto.password().isEmpty()) {
                throw new LoginException("비밀번호를 입력해 주세요.");
            }

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.email(),
                            requestDto.password(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws UnsupportedEncodingException {
        String email = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
//        AuthorityType role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getAuthority()
        String nickname = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getNickname();
        String role = "USER";
        GeneratedToken tokens = jwtUtil.generateToken(email, String.valueOf(role), nickname);
        insertInHeaderWithAccessToken(response, tokens);
        insertSetCookieWithRefreshToken(response, tokens);

        CustomResponseUtil.success(response, null);
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        if (failed instanceof LoginException) {
            CustomResponseUtil.fail(response, failed.getMessage(), HttpStatus.BAD_REQUEST);
        } else {
            CustomResponseUtil.fail(response, "아이디 또는 비밀번호가 틀렸습니다.", HttpStatus.FORBIDDEN);
        }
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setSecure(true); //https에 추가
        cookie.setAttribute("SameSite", "None");
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }

    private void insertSetCookieWithRefreshToken(HttpServletResponse response, GeneratedToken tokens) throws UnsupportedEncodingException {
        String refreshToken = URLEncoder.encode(tokens.getRefreshToken(), "utf-8");
        Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken);
        response.addCookie(refreshTokenCookie); // 쿠키를 응답에 추가
    }

    private static void insertInHeaderWithAccessToken(HttpServletResponse response, GeneratedToken tokens) {
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, tokens.getAccessToken());
        response.setHeader(JwtUtil.AUTHORIZATION_HEADER, tokens.getAccessToken());
    }
}
