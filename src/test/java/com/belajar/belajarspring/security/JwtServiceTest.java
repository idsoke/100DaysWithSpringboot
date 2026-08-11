package com.belajar.belajarspring.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "ZmFrZS1zZWNyZXQta2V5LXVudHVrLWJlbGFqYXItc3ByaW5nLWJvb3QtaGFyaS0xNi1qd3QtYXV0aGVudGljYXRpb24=";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000);
        userDetails = User.withUsername("admin")
                .password("irrelevant")
                .roles("ADMIN")
                .build();
    }

    @Test
    void generateToken_thenExtractUsername_shouldMatchOriginalUser() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void isTokenValid_withMatchingUser_shouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUser_shouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.withUsername("someone-else")
                .password("irrelevant")
                .roles("USER")
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void extractUsername_withExpiredToken_shouldThrowException() {
        JwtService shortLivedJwtService = new JwtService(SECRET, -1000);
        String token = shortLivedJwtService.generateToken(userDetails);

        assertThatThrownBy(() -> shortLivedJwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
