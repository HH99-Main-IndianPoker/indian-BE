package com.service.indianfrog.global.security.oauth2;

import com.service.indianfrog.global.jwt.JwtUtil;
import com.service.indianfrog.global.security.dto.GeneratedToken;
import com.service.indianfrog.global.security.filter.CustomResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // OAuth2User로 캐스팅하여 인증된 사용자 정보를 가져온다.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // 사용자 이메일을 가져온다.
        String email = oAuth2User.getAttribute("email");
        // 서비스 제공 플랫폼(GOOGLE, KAKAO, NAVER)이 어디인지 가져온다.
        String provider = oAuth2User.getAttribute("provider");

        // CustomOAuth2UserService에서 셋팅한 로그인한 회원 존재 여부를 가져온다.
        boolean isExist = oAuth2User.getAttribute("exist");
        // OAuth2User로 부터 Role을 얻어온다.
        String role = oAuth2User.getAuthorities().stream().
                findFirst() // 첫번째 Role을 찾아온다.
                .orElseThrow(IllegalAccessError::new) // 존재하지 않을 시 예외를 던진다.
                .getAuthority(); // Role을 가져온다.


        // 회원이 존재할경우
        if (isExist) {

            GeneratedToken tokens = jwtUtil.generateToken(email, role);
            response.addHeader(JwtUtil.AUTHORIZATION_HEADER, tokens.getAccessToken());
            response.setHeader(JwtUtil.AUTHORIZATION_HEADER, tokens.getAccessToken());

            String refreshToken = URLEncoder.encode(tokens.getRefreshToken(), "utf-8");
            Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken);
            response.addCookie(refreshTokenCookie); // 쿠키를 응답에 추가

            CustomResponseUtil.success(response, null);
        } else {
            // 새로 생성된 사용자의 토큰 생성 및 전달
            GeneratedToken tokens = jwtUtil.generateToken(email, role);
            response.addHeader(JwtUtil.AUTHORIZATION_HEADER, tokens.getAccessToken());
            response.setHeader(JwtUtil.AUTHORIZATION_HEADER, tokens.getAccessToken());

            String refreshToken = URLEncoder.encode(tokens.getRefreshToken(), "utf-8");
            Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken);
            response.addCookie(refreshTokenCookie);

            CustomResponseUtil.success(response, null);
        }
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        //cookie.setSecure(true); https에 추가
        cookie.setPath("/");

        return cookie;
    }
}
