package com.enterprise.hrm.auth.repository;

import com.enterprise.hrm.auth.entity.RefreshToken;
import com.enterprise.hrm.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * REFRESH TOKEN REPOSITORY
 * ============================================================
 *
 * @Modifying + @Query:
 *   When a JPQL query performs UPDATE or DELETE (not SELECT),
 *   you must annotate with @Modifying to tell Spring Data
 *   this is a write operation, not a read.
 *
 *   @Transactional is also required on the calling service method
 *   when using @Modifying queries.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by its token string.
     * Called when client sends a refresh request.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find refresh token by the owning user.
     * Used to check if user already has a refresh token (for upsert logic).
     */
    Optional<RefreshToken> findByUser(User user);

    /**
     * Delete all refresh tokens for a given user.
     * Called on logout to invalidate all sessions.
     *
     * @Modifying — indicates this is a DML statement (DELETE)
     * @Query — custom JPQL (more control than method naming)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(User user);
}
