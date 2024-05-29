package com.service.indianfrog.domain.mypage.controller;


import com.service.indianfrog.domain.mypage.dto.MyPageDto.*;
import com.service.indianfrog.domain.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/myPage")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping
    public ResponseEntity<MyPageInfo> getMyPage(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(myPageService.getMyPage(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<MyProfile> updateProfileImg(@AuthenticationPrincipal UserDetails userDetails, @RequestPart MultipartFile userImg) throws IOException {
        return ResponseEntity.ok(myPageService.updateProfileImg(userDetails.getUsername(), userImg));
    }

    @PostMapping("/point")
    public ResponseEntity<PointChange> pointRecharge(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PointChange pointChange){
        return ResponseEntity.ok(myPageService.pointRecharge(userDetails.getUsername(), pointChange.point()));
    }
}
