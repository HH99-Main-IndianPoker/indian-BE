package com.service.indianfrog.global.security.token;

import static com.service.indianfrog.global.exception.ErrorCode.NOT_EXPIRED_ACCESS_TOKEN;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private Key accessKey;

    public RefreshTokenService(RedisTemplate redisTemplate, JwtUtil jwtUtil,
        UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    private static final String ACCESS_TOKEN_KEY_PREFIX = "accessToken:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";

    @Transactional
    public void removeTokens(String accessToken, HttpServletRequest request, HttpServletResponse response) {
        Claims claims = extractClaims(accessToken.substring(7));
        String email = claims.getSubject();
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + email;
        redisTemplate.delete(refreshTokenKey);

        CookieDelete(request, response);
    }

    /*1.액세스 토큰으로 Refresh 토큰 객체를 조회
     * 2.RefreshToken 객체를 꺼내온다.
     * 3.email, role 를 추출해 새로운 액세스토큰을 만들어 반환*/
    @Transactional
    public String republishAccessTokenWithRotate(String accessToken,HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        Claims claims = extractClaims(accessToken.substring(7));
        String email = claims.getSubject();

        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX+ email;
        String refreshToken = (String) redisTemplate.opsForValue().get(refreshTokenKey);

        if (jwtUtil.verifyRefreshToken(refreshToken) != TokenVerificationResult.EXPIRED) {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND_USER.getMessage()));
            /*
             * 1.remove accesstoken
             * 2.generate tokens
             * 3.update each token*/

            removeTokens(email,request,response);
            GeneratedToken updatedToken = jwtUtil.generateToken(user.getEmail(),
                "USER", user.getNickname());
            response.addHeader(JwtUtil.AUTHORIZATION_HEADER, updatedToken.getAccessToken());
            response.setHeader(JwtUtil.AUTHORIZATION_HEADER, updatedToken.getAccessToken());

            String updatedRefreshToken = URLEncoder.encode(updatedToken.getRefreshToken(),
                "utf-8");
            Cookie refreshTokenCookie = createCookie("refreshTokenInfo", updatedRefreshToken);
            response.addCookie(refreshTokenCookie); // 쿠키를 응답에 추가

            return updatedToken.getAccessToken();
        }
        /*
         * 1.지금은 만료안된 엑세스토큰*/
        throw new RestApiException(NOT_EXPIRED_ACCESS_TOKEN.getMessage());
    }

    private Claims extractClaims(String accessToken) {
        try {
            log.error("accessToken not expired");
            throw new RestApiException(NOT_EXPIRED_ACCESS_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
           return e.getClaims();
        }
    }

    private static void CookieDelete(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    cookie.setValue(""); // 쿠키의 값을 비웁니다.
                    cookie.setPath("/"); // 쿠키의 경로를 설정합니다. 생성할 때의 경로와 일치해야 합니다.
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
