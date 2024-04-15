package com.service.indianfrog.global.security.token;

import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import com.service.indianfrog.global.jwt.JwtUtil;
import com.service.indianfrog.global.jwt.TokenVerificationResult;
import com.service.indianfrog.global.security.dto.GeneratedToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Transactional
    public void removeRefreshToken(String accessToken) {
        RefreshToken token = tokenRepository.findByAccessToken(accessToken)
                .orElseThrow(IllegalArgumentException::new);

        tokenRepository.delete(token);
    }

    /*1.액세스 토큰으로 Refresh 토큰 객체를 조회
     * 2.RefreshToken 객체를 꺼내온다.
     * 3.email, role 를 추출해 새로운 액세스토큰을 만들어 반환*/
    @Transactional
    public String republishAccessTokenWithRotate(String accessToken, HttpServletResponse response) throws UnsupportedEncodingException {
        Optional<RefreshToken> refreshToken = tokenRepository.findByAccessToken(accessToken);
        log.info(String.valueOf(accessToken));

        /*
         * 1.지금은 만료안된 엑세스토큰*/

        if (refreshToken.isPresent()) {
            String originAccessToken = refreshToken.get().getAccessToken().substring(7);
            if (jwtUtil.verifyAccessToken(originAccessToken) == TokenVerificationResult.EXPIRED) {
                RefreshToken resultToken = refreshToken.get();
                Optional<User> user = userRepository.findByEmail(resultToken.getId());
                String role = String.valueOf(user);
                User userNickname = userRepository.findByNickname(user.get().getNickname());
                /*
                 * 1.remove accesstoken
                 * 2.generate tokens
                 * 3.update each token*/

                removeRefreshToken(refreshToken.get().getAccessToken());
                GeneratedToken updatedToken = jwtUtil.generateToken(resultToken.getId(), role, String.valueOf(userNickname));
                response.addHeader(JwtUtil.AUTHORIZATION_HEADER, updatedToken.getAccessToken());
                response.setHeader(JwtUtil.AUTHORIZATION_HEADER, updatedToken.getAccessToken());

                String updatedRefreshToken = URLEncoder.encode(updatedToken.getRefreshToken(), "utf-8");
                Cookie refreshTokenCookie = createCookie("refreshToken", updatedRefreshToken);
                response.addCookie(refreshTokenCookie); // 쿠키를 응답에 추가

                return updatedToken.getAccessToken();
            }
        }
        throw new RestApiException(ErrorCode.IMPOSSIBLE_UPDATE_REFRESH_TOKEN.getMessage());
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
