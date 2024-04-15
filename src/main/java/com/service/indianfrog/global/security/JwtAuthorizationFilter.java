package com.service.indianfrog.global.security;

import com.service.indianfrog.domain.user.repository.UserRepository;
import com.service.indianfrog.global.jwt.JwtUtil;
import com.service.indianfrog.global.jwt.TokenVerificationResult;
import com.service.indianfrog.global.security.dto.SecurityUserDto;
import com.service.indianfrog.global.security.filter.CustomResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().contains("token/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // request Header에서 AccessToken을 가져온다.
        String tokenValue = jwtUtil.getJwtFromHeader(request);

        if (StringUtils.hasText(tokenValue)) {
            if (jwtUtil.verifyAccessToken(tokenValue) == TokenVerificationResult.EXPIRED || jwtUtil.verifyAccessToken(tokenValue) == TokenVerificationResult.INVALID) {
                log.error("Token Error");
                CustomResponseUtil.fail(response, "JWT 유효성 검사 실패", HttpStatus.UNAUTHORIZED);
                return;
            }

            Claims info = jwtUtil.getUserInfoFromToken(tokenValue);

            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }

//        String tokenValue = request.getHeader("Authorization");
//
//        // 토큰 검사 생략(모두 허용 URL의 경우 토큰 검사 통과)
//        if (!StringUtils.hasText(tokenValue)) {
//            doFilter(request, response, filterChain);
//            return;
//        }
//
//        // AccessToken을 검증하고, 만료되었을경우 예외를 발생시킨다.
//        if (!jwtUtil.verifyToken(tokenValue)) {
//            throw new JwtException("Access Token 만료!");
//        }
//
//        // AccessToken의 값이 있고, 유효한 경우에 진행한다.
//        if (jwtUtil.verifyToken(tokenValue)) {
//
//            // AccessToken 내부의 payload에 있는 email로 user를 조회한다. 없다면 예외를 발생시킨다 -> 정상 케이스가 아님
//            User findMember = userRepository.findByEmail(jwtUtil.getUid(tokenValue))
//                    .orElseThrow(IllegalStateException::new);
//
//            // SecurityContext에 등록할 User 객체를 만들어준다.
//            SecurityUserDto userDto = SecurityUserDto.builder()
//                    .memberNo(findMember.getId())
//                    .email(findMember.getEmail())
//                    .role("ROLE_".concat(String.valueOf(findMember.getAuthority())))
//                    .nickname(findMember.getNickname())
//                    .build();
//
//            // SecurityContext에 인증 객체를 등록해준다.
//            Authentication auth = getAuthentication(userDto);
//            SecurityContextHolder.getContext().setAuthentication(auth);
//        }

        filterChain.doFilter(request, response);
    }

    public Authentication getAuthentication(SecurityUserDto member) {
        return new UsernamePasswordAuthenticationToken(member, "",
                List.of(new SimpleGrantedAuthority(member.role())));
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
