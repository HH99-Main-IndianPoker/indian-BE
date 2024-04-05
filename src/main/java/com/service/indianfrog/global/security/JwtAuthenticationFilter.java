package com.service.indianfrog.global.security;

import com.service.indianfrog.domain.user.entity.type.AuthorityType;
import com.service.indianfrog.global.exception.LoginException;
import com.service.indianfrog.global.jwt.JwtUtil;
import com.service.indianfrog.global.util.CustomResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static com.service.indianfrog.domain.user.dto.UserRequestDto.*;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/v1/user/login");
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
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        String email = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        AuthorityType role = ((UserDetailsImpl) authResult.getPrincipal()).getMember().getAuthority();

        String token = jwtUtil.createToken(email, role);
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
        response.setHeader(JwtUtil.AUTHORIZATION_HEADER, token);

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
}
