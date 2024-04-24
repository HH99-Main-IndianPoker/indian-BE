package com.service.indianfrog.global.security.oauth2;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Oauth2ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/proxy/{service}")
    public ResponseEntity<String> proxyRequest(@PathVariable String service) {
        String url = determineUrl(service);
        if (url.isEmpty()) {
            return ResponseEntity.badRequest().body("Unsupported service");
        }

        // 외부 서비스를 호출합니다.
        try {
            URI uri = new URI(url);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(uri);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during redirection: " + e.getMessage());
        }
    }

    private String determineUrl(String service) {
        switch (service) {
            case "naver":
//                return "http://localhost:8081/oauth2/authorization/naver";
                return "https://api.indianfrog.com/oauth2/authorization/naver";
            case "kakao":
//                return "http://localhost:8081/oauth2/authorization/kakao";
                return "https://api.indianfrog.com/oauth2/authorization/kakao";
            case "google":
//                return "http://localhost:8081/oauth2/authorization/google";
                return "https://api.indianfrog.com/oauth2/authorization/google";
            default:
                return "";
        }
    }
}
