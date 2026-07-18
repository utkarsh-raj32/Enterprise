package com.enterprise.hrm.auth.entity;

import com.enterprise.hrm.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * ============================================================
 * USER ENTITY — Spring Security Integration
 * ============================================================
 *
 * WHY implement UserDetails directly on the entity?
 *   This is the "Adapter" design pattern — our User entity adapts
 *   to Spring Security's UserDetails interface. The alternative
 *   is creating a separate UserDetailsImpl class that wraps User.
 *   The direct approach is cleaner for small-to-medium projects.
 *
 * @ManyToOne (User → Role):
 *   Many users can have the same role.
 *   One user has exactly one role (simplified RBAC).
 *   For complex permissions, consider @ManyToMany with a roles table.
 *
 *   fetch = FetchType.EAGER:
 *   WHY EAGER for roles?
 *   Spring Security calls getAuthorities() during authentication.
 *   If LAZY, a new database query would be needed outside the
 *   transaction (in JwtAuthenticationFilter), causing
 *   LazyInitializationException. EAGER loads roles with the user in
 *   one JOIN query.
 *
 * @JoinColumn(name = "role_id"):
 *   Specifies the foreign key column in the 'users' table.
 *   Without @JoinColumn, JPA uses the naming convention:
 *   <fieldName>_<referencedColumnName> → "role_id" anyway,
 *   but explicit is always better.
 *
 * RELATIONSHIP DIAGRAM:
 *   users (many) ── role_id ──→ (one) roles
 *
 * extends BaseEntity:
 *   Inherits created_at and updated_at audit columns.
 *
 * UserDetails contract methods:
 *   getAuthorities()    — return roles as GrantedAuthority
 *   getPassword()       — return hashed password for BCrypt comparison
 *   getUsername()       — return the login identifier (email)
 *   isAccountNonExpired()    — always true (not implemented)
 *   isAccountNonLocked()     — use 'enabled' field
 *   isCredentialsNonExpired() — always true (not implemented)
 *   isEnabled()         — whether account is active
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")    // DB-level uniqueness guarantee
    }
)
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    /**
     * @Column(unique = true) — enforced at JPA level
     * @UniqueConstraint above — enforced at DB DDL level
     * Both are needed: JPA for validation, DB for data integrity
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * BCrypt hashed password — never store plain text.
     * BCrypt output is always 60 characters.
     */
    @Column(nullable = false, length = 60)
    private String password;

    /**
     * @ManyToOne — Many users share one role
     * EAGER — load role immediately when user is loaded (needed for Spring Security)
     * @JoinColumn — foreign key in 'users' table pointing to 'roles.id'
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Whether this account is active.
     * Inactive accounts cannot log in even with correct credentials.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    // ─────────────────────────────────────────────────────────────
    // UserDetails INTERFACE IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns the user's roles as Spring Security GrantedAuthority objects.
     *
     * SimpleGrantedAuthority wraps a string role name.
     * Spring Security expects "ROLE_" prefix for hasRole() checks:
     *   hasRole("ADMIN") checks for authority "ROLE_ADMIN"
     *   hasAuthority("ROLE_ADMIN") checks for exact match
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName().name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Spring Security uses getUsername() as the principal identifier.
     * We use email as the unique login identifier.
     */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;   // Not implementing account expiry (can be added)
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled; // Locked = disabled account
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;   // Not implementing credential rotation
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
