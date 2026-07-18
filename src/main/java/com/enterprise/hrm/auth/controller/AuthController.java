package com.enterprise.hrm.auth.controller;

import com.enterprise.hrm.auth.dto.AuthResponse;
import com.enterprise.hrm.auth.dto.LoginRequest;
import com.enterprise.hrm.auth.dto.RegisterRequest;
import com.enterprise.hrm.auth.dto.TokenRefreshRequest;
import com.enterprise.hrm.auth.service.AuthService;
import com.enterprise.hrm.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * AUTH CONTROLLER
 * ============================================================
 *
 * @RestController:
 *   = @Controller + @ResponseBody
 *   @Controller — registers this as a Spring MVC controller bean
 *   @ResponseBody — all handler methods serialize return values to
 *   JSON automatically (via Jackson's HttpMessageConverter).
 *   Without @ResponseBody, Spring would look for a view template.
 *
 *   WHY @RestController over @Controller?
 *   For REST APIs that always return JSON/XML, @RestController
 *   eliminates the need to put @ResponseBody on every method.
 *
 * @RequestMapping("/api/v1/auth"):
 *   Base path for all endpoints in this controller.
 *   Versioning (/api/v1/) in the URL allows backward-compatible
 *   API evolution (add /api/v2/ later without breaking v1 clients).
 *
 * @Tag (Swagger):
 *   Groups these endpoints under "Authentication" in Swagger UI.
 *
 * @Operation (Swagger):
 *   Documents each endpoint's purpose in Swagger UI.
 *
 * ResponseEntity<T>:
 *   WHY use ResponseEntity instead of plain return?
 *   ResponseEntity allows full control over:
 *   • HTTP status code (200, 201, 400, 401, etc.)
 *   • Response headers (Location, Content-Type, etc.)
 *   • Response body
 *
 *   Return types: ResponseEntity<ApiResponse<AuthResponse>>
 *   Outer ResponseEntity — sets HTTP status
 *   Middle ApiResponse<T> — our uniform envelope
 *   Inner AuthResponse    — the actual data payload
 *
 * @Valid:
 *   Triggers Bean Validation on the @RequestBody DTO.
 *   If validation fails, MethodArgumentNotValidException is thrown
 *   and caught by GlobalExceptionHandler → 400 response.
 *
 * @AuthenticationPrincipal:
 *   Injects the currently authenticated UserDetails from the
 *   SecurityContextHolder. Spring Security resolves this automatically.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, token refresh, and logout APIs")
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/auth/register
    // ─────────────────────────────────────────────────────────────

    /**
     * Register a new user account.
     * Returns 201 Created with auth tokens so the user is
     * immediately logged in after registration (better UX).
     */
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and returns JWT access + refresh tokens"
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/v1/auth/register - Registering user: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created for resource creation
                .body(ApiResponse.success("User registered successfully", authResponse, HttpStatus.CREATED.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/auth/login
    // ─────────────────────────────────────────────────────────────

    /**
     * Authenticate with email and password.
     * Returns JWT access token + refresh token.
     */
    @Operation(
        summary = "Login",
        description = "Authenticate with email/password and receive JWT tokens"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/v1/auth/login - Login attempt for: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse, HttpStatus.OK.value())
        );
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/auth/refresh-token
    // ─────────────────────────────────────────────────────────────

    /**
     * Get a new access token using a valid refresh token.
     * Called by client when access token expires (401 response).
     */
    @Operation(
        summary = "Refresh access token",
        description = "Use a valid refresh token to get a new JWT access token"
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        log.info("POST /api/v1/auth/refresh-token");
        AuthResponse authResponse = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", authResponse, HttpStatus.OK.value())
        );
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/v1/auth/logout
    // ─────────────────────────────────────────────────────────────

    /**
     * Logout the currently authenticated user.
     * Invalidates the refresh token on the server side.
     *
     * @AuthenticationPrincipal injects the current user from
     * the SecurityContextHolder (populated by JwtAuthenticationFilter).
     */
    @Operation(
        summary = "Logout",
        description = "Invalidate the current user's refresh token"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("POST /api/v1/auth/logout - User: {}", currentUser.getUsername());
        authService.logout(currentUser.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully", HttpStatus.OK.value())
        );
    }
}
