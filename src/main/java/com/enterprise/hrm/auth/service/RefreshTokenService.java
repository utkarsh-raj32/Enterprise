package com.enterprise.hrm.auth.service;

import com.enterprise.hrm.auth.entity.RefreshToken;
import com.enterprise.hrm.auth.entity.User;
import com.enterprise.hrm.auth.repository.RefreshTokenRepository;
import com.enterprise.hrm.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * ============================================================
 * REFRESH TOKEN SERVICE
 * ============================================================
 *
 * Manages the lifecycle of server-side refresh tokens:
 *   • Creation (with UUID generation and expiry calculation)
 *   • Expiry verification
 *   • Deletion on logout
 *
 * WHY UUID for refresh tokens?
 *   • Cryptographically random — cannot be guessed or brute-forced
 *   • No structure — reveals no information about the user or system
 *   • Industry standard for opaque tokens (OAuth2 spec)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Creates or replaces a refresh token for the given user.
     * We use upsert logic — if user already has a token, replace it.
     * This handles re-login without leaving stale tokens in DB.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Delete existing token (if any) for this user
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                // UUID v4 — random, URL-safe, no meaningful structure
                .token(UUID.randomUUID().toString())
                // Expiry: current time + 7 days
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Find refresh token by its string value.
     * @throws BusinessException if not found (token was revoked or never existed)
     */
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                        "Refresh token not found. Please login again.",
                        "REFRESH_TOKEN_NOT_FOUND"
                ));
    }

    /**
     * Verifies that the refresh token has not expired.
     * If expired, deletes it from DB and throws an exception.
     *
     * Token rotation can be added here:
     *   - Delete old token
     *   - Return signal to generate new refresh token
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            // Token is expired — remove it from DB
            refreshTokenRepository.delete(token);
            log.warn("Refresh token expired for user: {}", token.getUser().getEmail());
            throw new BusinessException(
                    "Refresh token has expired. Please login again.",
                    "REFRESH_TOKEN_EXPIRED"
            );
        }
        return token;
    }

    /**
     * Deletes all refresh tokens for a user (logout).
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
