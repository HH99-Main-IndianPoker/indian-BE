package com.service.indianfrog.global.security.token;

import com.service.indianfrog.domain.user.entity.User;
import com.service.indianfrog.domain.user.entity.type.AuthorityType;
import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public String republishAccessToken(String accessToken) {
        Optional<RefreshToken> refreshToken = tokenRepository.findByAccessToken(accessToken);
        log.info(String.valueOf(refreshToken.get().getRefreshToken()));
        log.info(String.valueOf(accessToken));

        if (refreshToken.isPresent() && jwtUtil.verifyRefreshToken(refreshToken.get().getRefreshToken())) {
            /**/
            RefreshToken resultToken = refreshToken.get();
            Optional<User> user = userRepository.findByEmail(resultToken.getId());
            String role = String.valueOf(user);

            String newAccessToken = jwtUtil.generateAccessToken(resultToken.getId(), role);
            resultToken.updateAccessToken(newAccessToken);
            tokenRepository.save(resultToken);
            return newAccessToken;
        }

        throw new IllegalArgumentException("리프레시 토큰 발급시 문제발생");
    }
}
