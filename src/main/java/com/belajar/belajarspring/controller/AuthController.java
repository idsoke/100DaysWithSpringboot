package com.belajar.belajarspring.controller;

import com.belajar.belajarspring.dto.LoginRequest;
import com.belajar.belajarspring.dto.LoginResponse;
import com.belajar.belajarspring.dto.MessageResponse;
import com.belajar.belajarspring.dto.RefreshTokenRequest;
import com.belajar.belajarspring.entity.RefreshToken;
import com.belajar.belajarspring.entity.User;
import com.belajar.belajarspring.exception.TokenRefreshException;
import com.belajar.belajarspring.repository.UserRepository;
import com.belajar.belajarspring.security.JwtService;
import com.belajar.belajarspring.security.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           RefreshTokenService refreshTokenService,
                           UserDetailsService userDetailsService,
                           UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User terautentikasi tapi tidak ditemukan di database"));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    // Menukar refresh token yang masih valid dengan access token baru, sekaligus
    // merotasi refresh token-nya (lama dihapus, diganti yang baru) agar sekali pakai.
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException(request.getRefreshToken(),
                        "Refresh token tidak ditemukan"));

        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(storedToken);
        User user = verifiedToken.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtService.generateToken(userDetails);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(newAccessToken, newRefreshToken.getToken());
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException(request.getRefreshToken(),
                        "Refresh token tidak ditemukan"));

        refreshTokenService.deleteByUser(storedToken.getUser());
        return new MessageResponse("Logout berhasil");
    }
}
