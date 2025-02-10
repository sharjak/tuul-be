package com.tuul.test.auth.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Token {
    private String token;
    private LocalDateTime expiryDate;
}
