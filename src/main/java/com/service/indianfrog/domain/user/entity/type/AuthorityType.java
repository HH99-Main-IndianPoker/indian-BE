package com.service.indianfrog.domain.user.entity.type;

import lombok.Getter;

@Getter
public enum AuthorityType {

    USER(Authority.USER), ADMIN(Authority.ADMIN);

    private final String authority;

    AuthorityType(String authority) {
        this.authority = authority;
    }

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}
