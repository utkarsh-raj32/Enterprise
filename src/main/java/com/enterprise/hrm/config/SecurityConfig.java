package com.enterprise.hrm.config;

import com.enterprise.hrm.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * ============================================================
 * SPRING SECURITY CONFIGURATION
 * ============================================================
 *
 * @Configuration:
 *   Marks this as a Spring configuration class. Methods annotated
 *   with @Bean inside this class produce Spring-managed beans.
 *   Spring calls these methods to create beans and registers
 *   them in the ApplicationContext.
 *
 *   IMPORTANT: @Configuration uses CGLIB proxying — calling @Bean
 *   methods on a @Configuration class returns the SAME singleton
 *   instance (from the Spring container), not a new object each time.
 *   This is different from @Component, where calling a @Bean method
 *   would create a new instance.
 *
 * @EnableWebSecurity:
 *   Enables Spring Security's web security support.
 *   In Spring Boot 3.x, this is NOT strictly required (auto-configured),
 *   but it's explicit best practice to include it.
 *
 * @EnableMethodSecurity:
 *   Enables @PreAuthorize, @PostAuthorize, @Secured annotations
 *   on individual controller/service methods.
 *   Replaces the older @EnableGlobalMethodSecurity in Spring Boot 3.x.
 *
 *   Example usage:
 *     @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
 *     public EmployeeResponse createEmployee(...) { ... }
 *
 * @Bean:
 *   Declares a Spring bean (managed object). Spring calls this
 *   method during ApplicationContext startup and stores the
 *   returned object in the IoC container.
 *   Other beans can @Autowired / constructor-inject this bean.
 *
 * DEPENDENCY INJECTION — @RequiredArgsConstructor:
 *   Spring injects JwtAuthenticationFilter and UserDetailsService
 *   into this configuration class via constructor injection.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity                        // Enables @PreAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * PUBLIC ENDPOINTS — whitelist (no authentication required)
     * All other endpoints require a valid JWT token.
     */
    private static final String[] PUBLIC_URLS = {
        "/api/v1/auth/**",          // login, register, refresh-token
        "/swagger-ui/**",           // Swagger UI static resources
        "/swagger-ui.html",         // Swagger UI entry point
        "/api-docs/**",             // OpenAPI JSON spec
        "/actuator/health",         // Docker health check endpoint
        "/actuator/info"
    };

    /**
     * SECURITY FILTER CHAIN — the heart of Spring Security config.
     *
     * SecurityFilterChain defines which URLs are protected and HOW.
     * Spring Security processes requests through an ordered chain
     * of filters. Our JwtAuthenticationFilter is inserted before
     * UsernamePasswordAuthenticationFilter.
     *
     * REQUEST PROCESSING ORDER:
     *   1. JwtAuthenticationFilter    — validates JWT, sets SecurityContext
     *   2. UsernamePasswordAuthFilter — handles form login (we don't use this)
     *   3. ExceptionTranslationFilter — converts AccessDeniedException to 403
     *   4. FilterSecurityInterceptor  — checks if current auth meets requirements
     *
     * csrf().disable():
     *   CSRF (Cross-Site Request Forgery) protection is stateful —
     *   it relies on CSRF tokens stored in sessions. Since we're
     *   stateless (JWT), CSRF attacks are not possible, so we disable it.
     *   CSRF is only relevant for browser-based session authentication.
     *
     * sessionManagement(STATELESS):
     *   Tells Spring Security NOT to create or use HTTP sessions.
     *   Each request must carry authentication (JWT) independently.
     *   This enables horizontal scaling — any server instance can
     *   handle any request without shared session state.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS using the corsConfigurationSource bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable CSRF — not needed for stateless JWT API
            .csrf(AbstractHttpConfigurer::disable)

            // Define authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow public endpoints without authentication
                .requestMatchers(PUBLIC_URLS).permitAll()
                // Allow GET on departments for all authenticated users
                .requestMatchers(HttpMethod.GET, "/api/v1/departments/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                // Employee CRUD — ADMIN and HR only
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR")
                // Leave management — HR and ADMIN can manage, EMPLOYEE can apply
                .requestMatchers(HttpMethod.POST, "/api/v1/leaves/apply").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/leaves/*/approve",
                                                "/api/v1/leaves/*/reject").hasAnyRole("ADMIN", "HR")
                // Salary — ADMIN only
                .requestMatchers("/api/v1/salary/**").hasRole("ADMIN")
                // Attendance — HR and EMPLOYEE
                .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Stateless session — no HTTP session will be created or used
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // Insert JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AUTHENTICATION PROVIDER
     *
     * DaoAuthenticationProvider is the standard implementation that:
     *   1. Calls UserDetailsService.loadUserByUsername() to load the user
     *   2. Compares the provided password with the stored hashed password
     *      using PasswordEncoder
     *   3. Returns an authenticated token if successful
     *
     * WHY explicit AuthenticationProvider?
     *   Spring Boot would auto-configure one, but we need to connect
     *   our custom UserDetailsService and BCrypt password encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AUTHENTICATION MANAGER
     *
     * The AuthenticationManager is the entry point for authentication.
     * When we call authManager.authenticate(token), it delegates to
     * the registered AuthenticationProvider (our DaoAuthenticationProvider).
     *
     * We expose it as a bean so AuthController can inject it for
     * programmatic authentication during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * PASSWORD ENCODER — BCrypt
     *
     * BCrypt is the industry-standard password hashing algorithm:
     * • Adaptive — work factor (cost) can be increased as hardware gets faster
     * • Salted — each hash includes a random salt (prevents rainbow table attacks)
     * • Slow — deliberately computationally expensive to prevent brute force
     *
     * Default strength = 10 (2^10 = 1024 iterations)
     * Production recommendation: 12 (2^12 = 4096 iterations)
     *
     * NEVER store plain-text passwords. BCrypt one-way hashing means:
     * • You can verify a password against its hash
     * • You cannot reverse a hash back to the original password
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // strength=12 for production
    }

    /**
     * CORS CONFIGURATION
     * Allows our frontend (React/Vite) to communicate with the backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000", "https://enterprise-frontend.vercel.app")); // Add your frontend domains
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
