package com.enterprise.hrm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ============================================================
 * JWT SERVICE — Token Generation, Validation, Parsing
 * ============================================================
 *
 * JWT (JSON Web Token) Structure:
 *   header.payload.signature
 *
 *   Header:  { "alg": "HS256", "typ": "JWT" }
 *   Payload: { "sub": "user@email.com", "iat": 1234567890, "exp": 1234568790 }
 *   Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
 *
 * WHY JWT over sessions?
 *   • Stateless — server doesn't store session state (scales horizontally)
 *   • Self-contained — all claims (user info, roles) are in the token
 *   • Works across microservices — any service can validate the token
 *     with the shared secret (or public key for RS256)
 *
 * Algorithm: HS256 (HMAC with SHA-256)
 *   Symmetric — same key signs and verifies. Suitable for single-service.
 *   For microservices: prefer RS256 (asymmetric) so services can
 *   verify without knowing the signing key.
 *
 * @Value("${...}"):
 *   Injects values from application.yml into fields.
 *   Spring resolves these from the Environment at bean creation time.
 *   This avoids hardcoding sensitive values in source code.
 *
 * @Service:
 *   Marks this as a Spring-managed service bean.
 *   WHY @Service over @Component?
 *   Semantically, @Service signals "this is a service layer component".
 *   Technically they're identical — @Service is @Component with a name.
 *   Using the right stereotype improves code readability and allows
 *   future tooling (e.g., Spring Cloud) to treat them differently.
 *
 * SPRING BEAN LIFECYCLE (for interview):
 *   1. Instantiation   — Spring calls constructor
 *   2. Property Injection — @Value, @Autowired fields populated
 *   3. Aware interfaces — setBeanName(), setApplicationContext() called
 *   4. @PostConstruct  — init method runs
 *   5. In Use          — bean handles requests
 *   6. @PreDestroy     — cleanup before shutdown
 *   7. Destruction     — bean removed from context
 */
@Slf4j
@Service
public class JwtService {

    /**
     * @Value injects the secret key from application.yml.
     * The key is stored as a hex-encoded string and decoded to bytes.
     * PRODUCTION NOTE: Store this in environment variables or
     * AWS Secrets Manager / HashiCorp Vault — never in source code.
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Access token expiry in milliseconds.
     * Default: 15 minutes (900000 ms)
     * Short expiry reduces risk if token is compromised.
     */
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // ─────────────────────────────────────────────────────────────
    // TOKEN GENERATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Generates an access token for a given user.
     * We pass UserDetails (not User entity) to keep this service
     * decoupled from the domain layer — it only knows about
     * Spring Security's UserDetails abstraction.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a token with additional claims (custom payload data).
     *
     * CLAIMS are key-value pairs in the JWT payload.
     * Standard claims: "sub" (subject), "iat" (issued at), "exp" (expiry)
     * Custom claims: anything you add — roles, userId, etc.
     *
     * WHY add roles to the token?
     *   Allows API Gateway / other services to make authorization decisions
     *   without hitting the database or calling the auth service.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                // Custom claims (roles, userId) — added BEFORE subject
                .claims(extraClaims)
                // "sub" claim — the principal identifier (email in our case)
                .subject(userDetails.getUsername())
                // "iat" — issued-at timestamp
                .issuedAt(new Date(System.currentTimeMillis()))
                // "exp" — expiry timestamp
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // Sign with HMAC-SHA256 and our secret key
                .signWith(getSignInKey())
                // Build and serialize to compact JWT string
                .compact();
    }

    // ─────────────────────────────────────────────────────────────
    // TOKEN VALIDATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Validates a token by checking:
     *   1. The username in the token matches the provided UserDetails
     *   2. The token is not expired
     *
     * WHY both checks?
     *   A token could be for a different user (token theft scenario),
     *   or it could be expired. Both cases should be rejected.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token is expired: {}", ex.getMessage());
            return false;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("JWT validation error: {}", ex.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CLAIM EXTRACTION
    // ─────────────────────────────────────────────────────────────

    /**
     * Extracts the username (subject claim) from the token.
     * "sub" is the standard JWT claim for the principal identifier.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic claim extractor using a function reference.
     * claimsResolver is a Function<Claims, T> — e.g., Claims::getSubject
     * This avoids duplication across all extract* methods.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses and verifies the JWT signature.
     * If the signature is invalid or the token is tampered,
     * Jwts.parser() throws an exception here.
     *
     * verifyWith(key) — verifies the HMAC-SHA256 signature
     * build().parseSignedClaims(token) — parses the token and returns Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodes the hex-encoded secret key from application.yml
     * and creates a cryptographic SecretKey object for HMAC-SHA256.
     *
     * Keys.hmacShaKeyFor() validates that the key is long enough
     * for HS256 (minimum 256 bits = 32 bytes).
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
