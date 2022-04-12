package com.capstone.bobmate.config.auth.jwt;

public interface JwtProperties {
    String SECRET = "secret"; // 우리 서버만 알고 있는 비밀값
    int EXPIRATION_TIME = 600000; // 10분 (1/1000초)
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "Authorization";
}
