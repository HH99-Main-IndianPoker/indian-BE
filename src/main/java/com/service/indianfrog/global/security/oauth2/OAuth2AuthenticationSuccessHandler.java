package com.service.indianfrog.global.security.oauth2;

import com.service.indianfrog.global.jwt.JwtUtil;
import com.service.indianfrog.global.security.dto.GeneratedToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        /*
         * OAuth2User로 캐스팅하여 인증된 사용자 정보를 가져온다.
         * 사용자 이메일을 가져온다.
         * 서비스 제공 플랫폼(GOOGLE, KAKAO, NAVER)이 어디인지 가져온다.*/
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String provider = oAuth2User.getAttribute("provider");
        /*
         * CustomOAuth2UserService에서 셋팅한 로그인한 회원 존재 여부를 가져온다.
         * OAuth2User로 부터 Role을 얻어온다.*/

        boolean isExist = oAuth2User.getAttribute("exist");
        String role = oAuth2User.getAuthorities().stream().
            findFirst() // 첫번째 Role을 찾아온다.
            .orElseThrow(IllegalAccessError::new) // 존재하지 않을 시 예외를 던진다.
            .getAuthority(); // Role을 가져온다.

        // 회원이 존재하지 않는 경우

        GeneratedToken tokens = jwtUtil.generateToken(email, role, email);
        setResponseRefreshTokens(response, tokens);


        String targetUrl = UriComponentsBuilder.fromUriString("https://indianfrog.com/oauth2/success")
            .queryParam("accessToken", tokens.getAccessToken())
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUriString();
        getRedirectStrategy().sendRedirect(request,response,targetUrl);
    }

    /*TODO set localstorage를 하는가에대해서 물어보기*/
    private void setResponseRefreshTokens(HttpServletResponse response, GeneratedToken tokens)
        throws UnsupportedEncodingException {

        String refreshToken = URLEncoder.encode(tokens.getRefreshToken(), "utf-8");
        Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken);
        response.addCookie(refreshTokenCookie); // 쿠키를 응답에 추가
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
