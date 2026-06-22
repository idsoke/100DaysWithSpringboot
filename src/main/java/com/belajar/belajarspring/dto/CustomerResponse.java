package com.belajar.belajarspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
}
