package com.service.indianfrog.global.jwt;

import com.service.indianfrog.global.properties.JwtProperties;
import com.service.indianfrog.global.security.dto.GeneratedToken;
import com.service.indianfrog.global.security.token.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtil {

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";

    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    // 토큰 만료 = 24시간
    private final long TOKEN_TIME = 60 * 60 * 24 * 1000L;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private Key accessKey;
    private Key refreshKey;
    private final JwtProperties jwtProperties;
    private final TokenService tokenService;

    @PostConstruct
    protected void init() {
        byte[] accessBytes = Base64.getDecoder().decode(jwtProperties.getAccess());
        accessKey = Keys.hmacShaKeyFor(accessBytes);

        byte[] refreshBytes = Base64.getDecoder().decode(jwtProperties.getRefresh());
        refreshKey = Keys.hmacShaKeyFor(refreshBytes);
    }

    // 토큰에서 Email을 추출한다.
    public String getUid(String token) {
        return Jwts.parser().setSigningKey(accessKey).parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰에서 ROLE(권한)만 추출한다.
    public String getRole(String token) {
        return Jwts.parser().setSigningKey(accessKey).parseClaimsJws(token).getBody()
            .get("role", String.class);
    }

    public GeneratedToken generateToken(String email, String role, String nickname) {
        String refreshToken = generateRefreshToken(email, role, nickname);
        String accessToken = generateAccessToken(email, role, nickname);

        // 토큰을 Redis에 저장한다.
        tokenService.saveTokenInfo(email, refreshToken);
        return new GeneratedToken(accessToken, refreshToken);
    }

    public String generateRefreshToken(String email, String role, String nickname) {
        Date date = new Date();

        return Jwts.builder()
            .setSubject(email) // 사용자 식별자값(ID)
            .claim(AUTHORIZATION_KEY, role) // 사용자 권한
            .claim("nickname", nickname)
            .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
            .setIssuedAt(date) // 발급일
            .signWith(refreshKey, signatureAlgorithm) // 암호화 알고리즘
            .compact();
    }


    public String generateAccessToken(String email, String role, String nickname) {
        Date date = new Date();
        return BEARER_PREFIX +
            Jwts.builder()
                .setSubject(email) // 사용자 식별자값(ID)
                .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                .claim("nickname", nickname)
                .setExpiration(new Date(date.getTime() + TOKEN_TIME / 24/120)) // 1hour
                .setIssuedAt(date) // 발급일
                .signWith(accessKey, signatureAlgorithm) // 암호화 알고리즘
                .compact();
    }

    public TokenVerificationResult verifyAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token);
            return TokenVerificationResult.VALID;
        } catch (SecurityException | MalformedJwtException |
                 io.jsonwebtoken.security.SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT AccessToken 입니다.");
            return TokenVerificationResult.EXPIRED;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return TokenVerificationResult.INVALID;
    }

    public TokenVerificationResult verifyRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(token);
            return TokenVerificationResult.VALID;
        } catch (SecurityException | MalformedJwtException |
                 io.jsonwebtoken.security.SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT AccessToken 입니다.");
            return TokenVerificationResult.EXPIRED;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return TokenVerificationResult.INVALID;
    }


    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            log.error("Access Token Expired: " + e.getMessage());
            return e.getClaims();
        } catch (JwtException e) {
            log.error("JWT processing failed " + e.getMessage());
            throw new RuntimeException("JWT processing failed", e);
        }

    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String deletePrefix(String accessToken) {
        return accessToken.substring(7);
    }
}
