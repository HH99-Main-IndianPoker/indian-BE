package com.service.indianfrog.global.security.token;

import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import com.service.indianfrog.global.jwt.JwtUtil;
import com.service.indianfrog.global.jwt.TokenVerificationResult;
import com.service.indianfrog.global.security.dto.GeneratedToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public RefreshTokenService(RedisTemplate redisTemplate, JwtUtil jwtUtil,
        UserRepository userRepository, TokenBlacklistService tokenBlacklistService) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";

    @Transactional
    public void removeTokens(String accessToken, HttpServletRequest request,
        HttpServletResponse response) {
        String jwtFromHeader = jwtUtil.getJwtFromHeader(request);
        Claims claims = jwtUtil.getUserInfoFromToken(jwtFromHeader);
        String email = claims.getSubject();
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + email;
        redisTemplate.delete(refreshTokenKey);

        CookieDelete(request, response);
    }

    @Transactional
    public void republishAccessTokenWithRotate(String accessToken, HttpServletRequest request,
        HttpServletResponse response) throws UnsupportedEncodingException {
        String token = jwtUtil.deletePrefix(accessToken);
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new RestApiException(ErrorCode.IMPOSSIBLE_UPDATE_REFRESH_TOKEN.getMessage());
        }

        Claims claims;
        try {
            tokenBlacklistService.blackListToken(token);
            claims = jwtUtil.getUserInfoFromToken(token);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims(); // 만료된 토큰에서도 사용자 정보를 추출
        }

        String email = claims.getSubject();
        String refreshToken = (String) redisTemplate.opsForValue()
            .get(REFRESH_TOKEN_KEY_PREFIX + email);
        if (jwtUtil.verifyRefreshToken(refreshToken) == TokenVerificationResult.EXPIRED) {
            throw new RestApiException(ErrorCode.EXPIRED_REFRESH_TOKEN.getMessage());
        }
        redisTemplate.delete(refreshToken);
        CookieDelete(request, response);
        User user = userRepository.findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));
        GeneratedToken generatedToken = jwtUtil.generateToken(user.getEmail(), "USER",
            user.getNickname());
        updateResponseWithTokens(response, generatedToken);
    }

    @Transactional
    protected void updateResponseWithTokens(HttpServletResponse response,
        GeneratedToken generatedToken)
        throws UnsupportedEncodingException {
        response.setHeader("Authorization", generatedToken.getAccessToken());
        String updatedRefreshToken = URLEncoder.encode(generatedToken.getRefreshToken(), "utf-8");
        Cookie refreshTokenCookie = createCookie("refreshToken", updatedRefreshToken);
        response.addCookie(refreshTokenCookie);
    }

    private static void CookieDelete(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    cookie.setValue(""); // 쿠키의 값을 비웁니다.
                    cookie.setPath("/"); // 쿠키의 경로를 설정합니다. 생성할 때의 경로와 일치해야 합니다.
                    cookie.setSecure(true);
                    cookie.setAttribute("SameSite", "None");
                    cookie.setMaxAge(0); // 쿠키의 최대 수명을 0으로 설정하여 즉시 만료시킵니다.
                    cookie.setHttpOnly(true); // JavaScript 접근 방지
                    response.addCookie(cookie); // 수정된 쿠키를 응답에 추가합니다.
                }
            }
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
}
