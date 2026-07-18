package com.enterprise.hrm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ============================================================
 * JWT AUTHENTICATION FILTER
 * ============================================================
 *
 * @Component:
 *   Registers this as a Spring bean. @Component is the generic
 *   stereotype. We use it here (not @Service) because this is
 *   a servlet filter/infrastructure component, not a service.
 *
 *   WHY NOT @Service?
 *   @Service, @Repository, @Controller are specializations of
 *   @Component with additional semantic meaning. A filter is
 *   infrastructure, not a business service — @Component is correct.
 *
 * extends OncePerRequestFilter:
 *   Spring's OncePerRequestFilter guarantees this filter runs
 *   EXACTLY ONCE per request, even in forward/include scenarios
 *   (unlike plain Filter which can run multiple times).
 *
 * @RequiredArgsConstructor (Lombok):
 *   Generates a constructor with all 'final' fields as parameters.
 *   WHY constructor injection over @Autowired?
 *   • Immutability — final fields can't be changed after construction
 *   • Testability — you can inject mocks via constructor without Spring
 *   • Fail-fast — missing dependencies fail at startup, not at runtime
 *   • Spring best practice since Spring 4.3 — explicit dependencies
 *
 * FILTER CHAIN FLOW:
 *   Request → [JwtAuthenticationFilter] → [UsernamePasswordAuthFilter] → Controller
 *
 *   Our filter intercepts every request, extracts the JWT from the
 *   Authorization header, validates it, and populates the
 *   SecurityContext so Spring Security knows the current user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Constructor injection via @RequiredArgsConstructor
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Core filter method — called for every HTTP request.
     *
     * FLOW:
     *   1. Extract Authorization header
     *   2. Check "Bearer " prefix
     *   3. Extract JWT token
     *   4. Extract username from token
     *   5. If not already authenticated: load UserDetails from DB
     *   6. Validate token against UserDetails
     *   7. Create Authentication and store in SecurityContextHolder
     *   8. Continue filter chain
     *
     * @param request     The HTTP request
     * @param response    The HTTP response
     * @param filterChain Next filter in the chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. If no Authorization header or not a Bearer token, skip this filter
        //    The request will continue to the filter chain — Spring Security
        //    will return 401 if the endpoint requires authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the actual JWT (remove "Bearer " prefix — 7 characters)
        final String jwt = authHeader.substring(7);

        // 4. Extract username (email) from JWT claims
        String userEmail;
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception ex) {
            log.error("Failed to extract username from JWT: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Only authenticate if we have a username AND not already authenticated
        //    WHY check SecurityContextHolder?
        //    If we're already authenticated (e.g., from a previous filter),
        //    we don't need to hit the database again.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load full UserDetails from database
            // WHY load from DB? Token might be valid but the user might
            // have been deactivated/deleted since token issuance
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // 6. Validate token — checks username match AND expiry
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Create Authentication object
                //    UsernamePasswordAuthenticationToken is the standard
                //    Authentication implementation for username/password auth.
                //
                //    Constructor: (principal, credentials, authorities)
                //    credentials = null because we're authenticating via JWT,
                //    not username/password at this point
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,         // principal (UserDetails object)
                        null,                // credentials (null for JWT auth)
                        userDetails.getAuthorities()  // roles/permissions
                );

                // Enrich with request details (IP address, session info)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store in SecurityContextHolder — this is what Spring Security
                // checks to know if the current request is authenticated.
                // SecurityContextHolder uses ThreadLocal — each request thread
                // has its own SecurityContext.
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authenticated user: {} for URI: {}",
                        userEmail, request.getRequestURI());
            }
        }

        // 8. Pass request to next filter in chain
        filterChain.doFilter(request, response);
    }
}
