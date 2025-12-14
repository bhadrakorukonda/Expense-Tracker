package com.expense.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response containing JWT token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String name;
    
    /**
     * Create response with Bearer token
     */
    public static LoginResponse of(String token, Long userId, String email, String name) {
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .name(name)
                .build();
    }
}
