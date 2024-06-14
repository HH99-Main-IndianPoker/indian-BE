package com.service.indianfrog.domain.ranking.dto;


import java.util.List;

public class Ranking{

    public record GetRankingInfo(
            List<GetRanking> rankings,
            String userImg,
            String myNickname,
            int myRanking,
            int myPoint
    ){}

    public record GetRanking(
            String userImg,
            Integer ranking,
            String nickname,
            int point
    ){}
}