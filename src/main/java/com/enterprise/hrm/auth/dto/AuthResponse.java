package com.enterprise.hrm.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * ============================================================
 * AUTH RESPONSE DTO
 * ============================================================
 *
 * Returned after successful login or registration.
 * Contains:
 *   - Access token (JWT, short-lived: 15 min)
 *   - Refresh token (opaque UUID string, long-lived: 7 days)
 *   - Token type (always "Bearer")
 *   - Basic user info so the client doesn't need a separate /me call
 *
 * @JsonInclude(NON_NULL):
 *   Don't serialize null fields. If email is null (shouldn't happen),
 *   it won't appear in the JSON response.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private String refreshToken;

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
