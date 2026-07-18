package com.enterprise.hrm.auth.repository;

import com.enterprise.hrm.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * USER REPOSITORY
 * ============================================================
 *
 * @Repository:
 *   Marks this interface as a Spring Data repository bean.
 *   Spring Data JPA generates the implementation at runtime using
 *   JDK dynamic proxies — we never write SQL or implementation code.
 *
 *   Additionally, @Repository enables Spring's exception translation:
 *   Raw JDBC/Hibernate exceptions (SQLException, HibernateException)
 *   are translated to Spring's DataAccessException hierarchy.
 *   This decouples the service layer from persistence technology.
 *
 * extends JpaRepository<User, Long>:
 *   JpaRepository provides:
 *   • save(entity)       — INSERT or UPDATE (merge)
 *   • findById(id)       — SELECT by PK, returns Optional<T>
 *   • findAll()          — SELECT all
 *   • findAll(Pageable)  — SELECT with pagination
 *   • delete(entity)     — DELETE
 *   • count()            — SELECT COUNT(*)
 *   • existsById(id)     — SELECT EXISTS(...)
 *   And JPA-specific:
 *   • flush()            — force Hibernate to sync to DB
 *   • saveAndFlush()     — save + immediate flush
 *
 * QUERY DERIVATION — Spring Data method naming:
 *   findByEmail(String email)
 *   Spring parses this method name and generates:
 *   SELECT u FROM User u WHERE u.email = :email
 *
 *   No SQL, no @Query — just method naming conventions!
 *   Other examples:
 *   • findByEmailAndEnabled(email, true)
 *   • findByFirstNameContaining(name)
 *   • countByRole(role)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Returns Optional<User> — forces callers to handle the not-found case
     * instead of returning null (which would cause NullPointerExceptions).
     *
     * Generated JPQL: SELECT u FROM User u WHERE u.email = :email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with this email already exists.
     * Used during registration to prevent duplicates.
     * More efficient than findByEmail() as it doesn't load the full entity.
     *
     * Generated SQL: SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     */
    boolean existsByEmail(String email);
}
