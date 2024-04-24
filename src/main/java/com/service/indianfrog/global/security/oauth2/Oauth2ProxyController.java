package com.service.indianfrog.global.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Oauth2ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/oauth2/url/{service}")
    public ResponseEntity<Object> proxyRequest(@PathVariable String service) {
        String url = determineUrl(service);
        if (url.isEmpty()) {
            return ResponseEntity.badRequest().body("Unsupported service");
        }

        // 외부 서비스를 호출합니다.
        return ResponseEntity.ok(new UrlResponse(url));
    }

    private String determineUrl(String service) {
        switch (service) {
            case "naver":
//                return "http://localhost:8081/oauth2/authorization/naver";
                return "https://api.indianfrog.com/oauth2/authorization/naver";
            case "kakao":
                return "http://localhost:8081/oauth2/authorization/kakao";
//                return "https://api.indianfrog.com/oauth2/authorization/kakao";
            case "google":
//                return "http://localhost:8081/oauth2/authorization/google";
                return "https://api.indianfrog.com/oauth2/authorization/google";
            default:
                return "";
        }
    }

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
