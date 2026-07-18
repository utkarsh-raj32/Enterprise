package com.enterprise.hrm.auth.service;

import com.enterprise.hrm.auth.dto.AuthResponse;
import com.enterprise.hrm.auth.dto.LoginRequest;
import com.enterprise.hrm.auth.dto.RegisterRequest;
import com.enterprise.hrm.auth.dto.TokenRefreshRequest;

/**
 * ============================================================
 * AUTH SERVICE INTERFACE
 * ============================================================
 *
 * WHY define an interface + implementation?
 *   1. Loose coupling — controllers depend on the interface,
 *      not the implementation. Easy to swap implementations.
 *   2. Testability — Mockito can mock interfaces easily
 *   3. Spring AOP — @Transactional proxies work on interfaces
 *   4. DIP (Dependency Inversion Principle) — depend on abstractions
 *   5. Team development — multiple devs can work on impl separately
 *
 * INTERVIEW: "Why use interface + impl in service layer?"
 *   This is the Strategy design pattern. The interface defines
 *   the contract; the impl provides the algorithm. If we need
 *   to support OAuth or SAML in the future, we add a new impl
 *   without changing the controller.
 */
public interface AuthService {

    /**
     * Registers a new user, assigns role, and returns auth tokens.
     * @throws DuplicateResourceException if email is already in use
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates user with email/password and returns auth tokens.
     * @throws BadCredentialsException if email or password is wrong
     */
    AuthResponse login(LoginRequest request);

    /**
     * Uses a valid refresh token to issue a new access token.
     * @throws TokenRefreshException if refresh token is expired/invalid
     */
    AuthResponse refreshToken(TokenRefreshRequest request);

    /**
     * Invalidates the user's refresh token (logout).
     * @param email The email of the user logging out
     */
    void logout(String email);
}
