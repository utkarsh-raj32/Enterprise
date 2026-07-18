package com.enterprise.hrm.auth.dto;

import com.enterprise.hrm.auth.entity.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ============================================================
 * REGISTER REQUEST DTO
 * ============================================================
 *
 * WHY DTOs instead of exposing entities?
 *   1. Security — prevents mass assignment attacks
 *      (client can't set 'enabled', 'role' directly on entity)
 *   2. Versioning — API contract can evolve independently of DB schema
 *   3. Validation — @Valid constraints live on the DTO, not the entity
 *   4. Separation of concerns — entity models DB, DTO models API contract
 *
 * Bean Validation Annotations:
 *   @NotBlank  — not null AND not empty AND not whitespace-only
 *   @NotNull   — just not null (can be empty string)
 *   @Email     — must match email format (uses RFC 5321 pattern)
 *   @Size      — min/max length constraint
 *
 *   These are processed by Hibernate Validator (JSR-380 implementation)
 *   when @Valid is applied to a @RequestBody parameter in the controller.
 *
 * RECORD vs @Data DTO:
 *   Java 16+ records can be used for immutable DTOs.
 *   We use @Data (Lombok) for consistency and flexibility
 *   (records can't have @Valid on constructor params easily in older Spring).
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be 2-100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be 2-100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Role assignment — in production, ADMIN-only users should have
     * the role hardcoded or validated server-side.
     * Here we accept it from the request for flexibility.
     */
    @NotNull(message = "Role is required")
    private ERole role;
}
