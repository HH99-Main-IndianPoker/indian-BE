package com.service.indianfrog.global.security.dto;

public record SecurityUserDto(
        String email,
        String nickname,
        String picture,
        String role,
        Long memberNo

) {

}
