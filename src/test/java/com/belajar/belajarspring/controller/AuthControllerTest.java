package com.belajar.belajarspring.controller;

import com.belajar.belajarspring.config.SecurityConfig;
import com.belajar.belajarspring.dto.LoginRequest;
import com.belajar.belajarspring.dto.RefreshTokenRequest;
import com.belajar.belajarspring.entity.RefreshToken;
import com.belajar.belajarspring.entity.Role;
import com.belajar.belajarspring.entity.User;
import com.belajar.belajarspring.repository.UserRepository;
import com.belajar.belajarspring.security.JwtService;
import com.belajar.belajarspring.security.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private UserRepository userRepository;

    // Dependency dari JwtAuthenticationFilter yang ikut ter-scan sebagai bean Filter di @WebMvcTest
    @MockBean
    private UserDetailsService userDetailsService;

    private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_ADMIN);
        return user;
    }

    private RefreshToken sampleRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        return refreshToken;
    }

    @Test
    void login_withValidCredentials_shouldReturnAccessAndRefreshToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        UserBuilder principalBuilder = org.springframework.security.core.userdetails.User
                .withUsername("admin").password("admin123").roles("ADMIN");
        UserDetails principal = principalBuilder.build();
        User user = sampleUser();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null));
        when(jwtService.generateToken(any())).thenReturn("fake-jwt-token");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(refreshTokenService.createRefreshToken(user)).thenReturn(sampleRefreshToken(user, "fake-refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("fake-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_withBlankUsername_shouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_withValidToken_shouldReturnNewAccessAndRefreshToken() throws Exception {
        User user = sampleUser();
        RefreshToken storedToken = sampleRefreshToken(user, "old-refresh-token");
        RefreshToken rotatedToken = sampleRefreshToken(user, "new-refresh-token");

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");

        when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(Optional.of(storedToken));
        when(refreshTokenService.verifyExpiration(storedToken)).thenReturn(storedToken);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername("admin")
                        .password("irrelevant").roles("ADMIN").build());
        when(jwtService.generateToken(any())).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(rotatedToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refresh_withUnknownToken_shouldReturn403() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("unknown-token");

        when(refreshTokenService.findByToken("unknown-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_withKnownToken_shouldReturnOk() throws Exception {
        User user = sampleUser();
        RefreshToken storedToken = sampleRefreshToken(user, "some-refresh-token");

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("some-refresh-token");

        when(refreshTokenService.findByToken("some-refresh-token")).thenReturn(Optional.of(storedToken));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout berhasil"));
    }
}
