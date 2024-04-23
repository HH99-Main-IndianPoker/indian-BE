package com.service.indianfrog.global.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
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
        String url = "";
        switch (service) {
            case "naver":
                url = "https://api.indianfrog.com/oauth2/authorization/naver";
                break;
            case "kakao":
                url = "https://api.indianfrog.com/oauth2/authorization/kakao";
                break;
            case "google":
                url = "https://api.indianfrog.com/oauth2/authoizatioin/google";
                break;
            default:
                return ResponseEntity.badRequest().body("Unsupported service");
        }
        //외부 서비스를 호출합니다.
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        //결과반환
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

}
