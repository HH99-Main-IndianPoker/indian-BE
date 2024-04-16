package com.service.indianfrog.domain.mypage.controller;


import com.service.indianfrog.domain.mypage.dto.MyPageInfo;
import com.service.indianfrog.domain.mypage.dto.PointChange;
import com.service.indianfrog.domain.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/myPage")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping
    public ResponseEntity<MyPageInfo> GetMyPage(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(myPageService.getMyPage(userDetails.getUsername()));
    }

    @PostMapping("/point")
    public ResponseEntity<PointChange> PointRecharge(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PointChange pointChange){
        return ResponseEntity.ok(myPageService.pointRecharge(userDetails.getUsername(), pointChange.point()));
    }

}
