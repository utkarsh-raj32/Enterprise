package com.enterprise.hrm.auth;

import com.enterprise.hrm.auth.dto.AuthResponse;
import com.enterprise.hrm.auth.dto.LoginRequest;
import com.enterprise.hrm.auth.dto.RegisterRequest;
import com.enterprise.hrm.auth.entity.ERole;
import com.enterprise.hrm.auth.entity.Role;
import com.enterprise.hrm.auth.entity.RefreshToken;
import com.enterprise.hrm.auth.entity.User;
import com.enterprise.hrm.auth.repository.RoleRepository;
import com.enterprise.hrm.auth.repository.UserRepository;
import com.enterprise.hrm.auth.service.AuthServiceImpl;
import com.enterprise.hrm.auth.service.RefreshTokenService;
import com.enterprise.hrm.exception.DuplicateResourceException;
import com.enterprise.hrm.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Auth Service Unit Tests.
 * Tests registration, login, and token refresh flows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role adminRole;
    private User mockUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().id(1L).name(ERole.ADMIN).build();

        mockUser = User.builder()
                .id(1L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@enterprise.com")
                .password("$2a$12$hashedpassword")
                .role(adminRole)
                .enabled(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Admin");
        registerRequest.setLastName("User");
        registerRequest.setEmail("admin@enterprise.com");
        registerRequest.setPassword("Admin@123");
        registerRequest.setRole(ERole.ADMIN);
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should register user and return tokens on success")
        void shouldRegisterSuccessfully() {
            // ARRANGE
            RefreshToken refreshToken = RefreshToken.builder()
                    .token("refresh-token-uuid")
                    .user(mockUser)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build();

            given(userRepository.existsByEmail("admin@enterprise.com")).willReturn(false);
            given(roleRepository.findByName(ERole.ADMIN)).willReturn(Optional.of(adminRole));
            given(passwordEncoder.encode("Admin@123")).willReturn("$2a$12$hashedpassword");
            given(userRepository.save(any(User.class))).willReturn(mockUser);
            given(jwtService.generateToken(any(User.class))).willReturn("jwt-access-token");
            given(refreshTokenService.createRefreshToken(any(User.class))).willReturn(refreshToken);

            // ACT
            AuthResponse response = authService.register(registerRequest);

            // ASSERT
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
            assertThat(response.getEmail()).isEqualTo("admin@enterprise.com");
            assertThat(response.getRole()).isEqualTo("ADMIN");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {
            // ARRANGE
            given(userRepository.existsByEmail("admin@enterprise.com")).willReturn(true);

            // ACT & ASSERT
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");

            // Verify password encoder and save were NEVER called
            then(passwordEncoder).should(never()).encode(anyString());
            then(userRepository).should(never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should return auth tokens on successful login")
        void shouldLoginSuccessfully() {
            // ARRANGE
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("admin@enterprise.com");
            loginRequest.setPassword("Admin@123");

            RefreshToken refreshToken = RefreshToken.builder()
                    .token("new-refresh-token")
                    .user(mockUser)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

            given(authenticationManager.authenticate(any())).willReturn(authToken);
            given(jwtService.generateToken(mockUser)).willReturn("new-access-token");
            given(refreshTokenService.createRefreshToken(mockUser)).willReturn(refreshToken);

            // ACT
            AuthResponse response = authService.login(loginRequest);

            // ASSERT
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        }
    }
}
