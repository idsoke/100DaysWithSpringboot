package com.belajar.belajarspring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token tidak boleh kosong")
    private String refreshToken;
}
