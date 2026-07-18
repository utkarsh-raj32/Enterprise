package com.enterprise.hrm.auth.service;

import com.enterprise.hrm.auth.dto.AuthResponse;
import com.enterprise.hrm.auth.dto.LoginRequest;
import com.enterprise.hrm.auth.dto.RegisterRequest;
import com.enterprise.hrm.auth.dto.TokenRefreshRequest;
import com.enterprise.hrm.auth.entity.ERole;
import com.enterprise.hrm.auth.entity.RefreshToken;
import com.enterprise.hrm.auth.entity.Role;
import com.enterprise.hrm.auth.entity.User;
import com.enterprise.hrm.auth.repository.RoleRepository;
import com.enterprise.hrm.auth.repository.UserRepository;
import com.enterprise.hrm.exception.BusinessException;
import com.enterprise.hrm.exception.DuplicateResourceException;
import com.enterprise.hrm.exception.ResourceNotFoundException;
import com.enterprise.hrm.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================
 * AUTH SERVICE IMPLEMENTATION
 * ============================================================
 *
 * @Service:
 *   Specialization of @Component indicating this is a service
 *   bean in the business logic layer.
 *   Spring creates a singleton instance and manages its lifecycle.
 *
 * @Transactional:
 *   Class-level @Transactional means ALL public methods run in
 *   a transaction by default. Individual methods can override with
 *   their own @Transactional(readOnly=true, propagation=...).
 *
 *   TRANSACTION PROPAGATION:
 *   • REQUIRED (default) — join existing transaction or create new
 *   • REQUIRES_NEW — always create new transaction (suspend current)
 *   • SUPPORTS — use transaction if one exists, else non-transactional
 *   • NEVER — throw if transaction exists
 *   • NESTED — savepoint within existing transaction
 *
 *   ROLLBACK BEHAVIOR:
 *   @Transactional rolls back on RuntimeException and Error by default.
 *   For checked exceptions: @Transactional(rollbackFor = Exception.class)
 *
 * @Slf4j (Lombok):
 *   Injects: private static final Logger log = LoggerFactory.getLogger(...)
 *   WHY SLF4J over java.util.logging?
 *   SLF4J is a facade — works with Logback, Log4j2, JUL etc.
 *   Log levels: TRACE < DEBUG < INFO < WARN < ERROR
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    // All dependencies injected via constructor (@RequiredArgsConstructor)
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * REGISTER FLOW:
     *   1. Check if email already exists
     *   2. Fetch role from DB (ADMIN/HR/EMPLOYEE)
     *   3. Hash password with BCrypt
     *   4. Save user
     *   5. Generate JWT access token
     *   6. Generate refresh token
     *   7. Return AuthResponse
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // 1. Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // 2. Fetch the requested role — EMPLOYEE by default in production
        ERole roleEnum = request.getRole() != null ? request.getRole() : ERole.EMPLOYEE;
        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleEnum));

        // 3. Build and save user with BCrypt-hashed password
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
                .role(role)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // 4. Generate tokens
        String accessToken  = jwtService.generateToken(savedUser);
        RefreshToken refresh = refreshTokenService.createRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, refresh.getToken());
    }

    /**
     * LOGIN FLOW:
     *   1. Authenticate via Spring Security's AuthenticationManager
     *      (calls UserDetailsService → DaoAuthenticationProvider → BCrypt compare)
     *   2. On success, generate JWT access token
     *   3. Generate/renew refresh token
     *   4. Return AuthResponse
     *
     * WHY use AuthenticationManager instead of manual comparison?
     *   AuthenticationManager is Spring Security's official authentication
     *   entry point. It handles:
     *   • Loading UserDetails from DB
     *   • Comparing BCrypt hashed passwords
     *   • Throwing BadCredentialsException on failure
     *   • Firing AuthenticationSuccessEvent on success
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // This internally calls UserDetailsServiceImpl.loadUserByUsername()
        // and compares passwords using BCryptPasswordEncoder.
        // Throws BadCredentialsException on failure (handled by GlobalExceptionHandler)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach here, authentication succeeded
        User user = (User) authentication.getPrincipal();
        log.info("User logged in successfully: {}", user.getEmail());

        String accessToken = jwtService.generateToken(user);
        RefreshToken refresh = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refresh.getToken());
    }

    /**
     * REFRESH TOKEN FLOW:
     *   1. Find refresh token in DB
     *   2. Check it's not expired
     *   3. Issue new access token for the token's owner
     *
     * We do NOT rotate the refresh token here (can be added for enhanced security).
     * Token rotation: delete old refresh token, issue new one with each use.
     */
    @Override
    @Transactional(readOnly = false)
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        log.info("Processing token refresh request");

        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
    }

    /**
     * LOGOUT FLOW:
     *   Deletes the user's refresh token from the database.
     *   The access token remains valid until it expires (15 min).
     *
     *   For immediate access token invalidation:
     *   • Maintain a token denylist (Redis set)
     *   • Or use very short access token expiry (1-5 min)
     */
    @Override
    public void logout(String email) {
        log.info("Logging out user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        refreshTokenService.deleteByUser(user);
        log.info("User logged out, refresh token invalidated: {}", email);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getName().name())
                .build();
    }
}
