package com.service.indianfrog.domain.mypage.dto;

public class MyPageDto {

    public record MyPageInfo (

            String nickName,
            String email,
            int ranking,
            int point,
            String myImageUrl

    ) {}

    public record MyProfile(String userImgUrl) {}

    public record PointChange (
            int point
    ){}

}
