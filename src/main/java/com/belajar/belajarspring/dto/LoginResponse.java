package com.belajar.belajarspring.dto;

public record LoginResponse(String token, String refreshToken, String tokenType) {
    public LoginResponse(String token, String refreshToken) {
        this(token, refreshToken, "Bearer");
    }
}
