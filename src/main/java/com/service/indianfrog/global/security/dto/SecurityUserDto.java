package com.service.indianfrog.global.security.dto;

import lombok.*;

public record SecurityUserDto(
        String email,
        String nickname,
        String picture,
        String role,
        Long memberNo

) {

}
