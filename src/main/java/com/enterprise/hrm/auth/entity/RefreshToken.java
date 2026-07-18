package com.enterprise.hrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * ============================================================
 * REFRESH TOKEN ENTITY
 * ============================================================
 *
 * WHY Refresh Tokens?
 *   JWT access tokens have short expiry (15 min) for security.
 *   Requiring users to re-login every 15 minutes is bad UX.
 *   Refresh tokens solve this:
 *
 *   1. Login → get access token (15 min) + refresh token (7 days)
 *   2. Access token expires → use refresh token to get NEW access token
 *   3. Refresh token expires → user must log in again
 *
 * WHY store refresh tokens in DB (not as JWT)?
 *   Server-side storage allows:
 *   • Invalidation on logout (delete from DB)
 *   • Token rotation (issue new refresh token with each use)
 *   • Revocation if compromised
 *
 *   JWTs are stateless — you can't invalidate a JWT without
 *   a denylist or token versioning.
 *
 * @OneToOne (RefreshToken → User):
 *   One user has at most one active refresh token.
 *   WHY not OneToMany? One refresh token per user keeps it simple
 *   and forces logout from old sessions on re-login.
 *
 * expiryDate as Instant:
 *   Instant is timezone-independent (UTC nanoseconds since epoch).
 *   Better than LocalDateTime for expiry comparisons.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The opaque token string — generated as UUID (random, unpredictable).
     * @Column(unique = true) — ensures no two users share a token.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /**
     * @OneToOne — One refresh token belongs to exactly one user
     * LAZY — we only need the user when validating the token
     * @JoinColumn — FK column 'user_id' in refresh_tokens table
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    /**
     * UTC timestamp when this refresh token expires.
     * Checked in RefreshTokenService.verifyExpiration()
     */
    @Column(nullable = false)
    private Instant expiryDate;
}
