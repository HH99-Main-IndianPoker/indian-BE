package com.service.indianfrog.domain.ranking.dto;


import java.util.List;

public class Ranking{

    public record GetRankingInfo(
            List<GetRanking> rankings,

            String myNickname,
            int myRanking,
            int myPoint
    ){}

    public record GetRanking(
            int ranking,
            String nickname,
            int point
    ){}
}