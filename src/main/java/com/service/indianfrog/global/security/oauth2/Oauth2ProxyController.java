package com.service.indianfrog.global.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class Oauth2ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    // Naver OAuth2 URL 요청
    @GetMapping("/oauth2/url/naver")
    public ResponseEntity<Object> naverOAuth2Url() {
        String url = "https://api.indianfrog.com/oauth2/authorization/naver";
        return ResponseEntity.ok(new UrlResponse(url));
    }

    // Kakao OAuth2 URL 요청
    @GetMapping("/oauth2/url/kakao")
    public ResponseEntity<Object> kakaoOAuth2Url() {
        log.debug("Requesting Kakao OAuth2 URL");
//        String url = "http://127.0.0.1:8081/oauth2/authorization/kakao";
        String url = "https://api.indianfrog.com/oauth2/authorization/kakao";
        log.debug("Redirecting to Kakao authorization page at {}", url);
        return ResponseEntity.ok(new UrlResponse(url));
    }

    // Google OAuth2 URL 요청
    @GetMapping("/oauth2/url/google")
    public ResponseEntity<Object> googleOAuth2Url() {
        String url = "https://api.indianfrog.com/oauth2/authorization/google";
        return ResponseEntity.ok(new UrlResponse(url));
    }
    // URL Response 클래스
    private static class UrlResponse {
        private String url;

        public UrlResponse(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
