package com.enterprise.hrm.security;

import com.enterprise.hrm.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================
 * USER DETAILS SERVICE IMPLEMENTATION
 * ============================================================
 *
 * UserDetailsService is a core Spring Security interface with
 * a single method: loadUserByUsername(String username).
 *
 * Spring Security calls this during authentication to:
 *   1. Fetch the user from the database by username (email in our case)
 *   2. Return a UserDetails object containing:
 *      - Username, password (hashed), authorities (roles), account flags
 *
 * WHY implement this interface?
 *   Spring Security's DaoAuthenticationProvider uses this service
 *   to retrieve user information for authentication comparison.
 *   Our User entity implements UserDetails directly, so we simply
 *   return the User object — no mapping needed.
 *
 * @Transactional:
 *   Wraps the method in a database transaction.
 *   WHY here? Our User entity has LAZY-loaded collections (roles).
 *   If accessed outside a transaction (after session close), this
 *   would throw LazyInitializationException.
 *   The transaction ensures the session stays open while loading
 *   the user and its authorities.
 *
 *   readOnly = true: Hint to the database that this is a read operation.
 *   MySQL can optimize read-only transactions (no undo log creation).
 *   Also prevents accidental data modifications.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @param username — In our system, this is the user's EMAIL address
     *                   (we use email as the unique login identifier, not username)
     * @throws UsernameNotFoundException — Spring Security catches this and
     *   translates it to a BadCredentialsException to avoid leaking
     *   whether the account exists (security best practice)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not found with email: " + username)
                );
    }
}
