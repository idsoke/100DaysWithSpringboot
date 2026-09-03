package com.belajar.belajarspring.security;

import com.belajar.belajarspring.entity.RefreshToken;
import com.belajar.belajarspring.entity.Role;
import com.belajar.belajarspring.entity.User;
import com.belajar.belajarspring.exception.TokenRefreshException;
import com.belajar.belajarspring.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;
    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, REFRESH_EXPIRATION_MS);
        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_ADMIN);
    }

    @Test
    void createRefreshToken_shouldDeleteOldTokenAndSaveNewOne() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        verify(refreshTokenRepository).deleteByUser(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getToken()).isNotBlank();
        assertThat(captor.getValue().getExpiryDate()).isAfter(Instant.now());
        assertThat(result.getToken()).isEqualTo(captor.getValue().getToken());
    }

    @Test
    void verifyExpiration_withNonExpiredToken_shouldReturnSameToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiryDate(Instant.now().plusSeconds(60));

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isEqualTo(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_withExpiredToken_shouldDeleteAndThrow() {
        RefreshToken token = new RefreshToken();
        token.setToken("expired-token");
        token.setUser(user);
        token.setExpiryDate(Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("kedaluwarsa");

        verify(refreshTokenRepository).delete(token);
    }
}
