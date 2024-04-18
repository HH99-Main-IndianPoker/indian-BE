package com.service.indianfrog.domain.mypage.dto;

import org.springframework.web.multipart.MultipartFile;

public record MyPageInfo (

    String nickName,
    String email,
    int ranking,
    int point,
    String myImageUrl

    ) {

}
