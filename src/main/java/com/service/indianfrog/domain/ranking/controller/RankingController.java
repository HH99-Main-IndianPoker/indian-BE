package com.service.indianfrog.domain.ranking.controller;

import com.service.indianfrog.domain.ranking.dto.Ranking.*;
import com.service.indianfrog.domain.ranking.service.RankingService;
import com.service.indianfrog.global.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public ResponseEntity<GetRankingInfo> getRanking(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(rankingService.getRanking(userDetails.getUsername()));
    }


}
