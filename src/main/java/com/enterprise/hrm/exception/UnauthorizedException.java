package com.enterprise.hrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ============================================================
 * UNAUTHORIZED EXCEPTION (401)
 * ============================================================
 *
 * Thrown when a user attempts to access a resource without
 * valid credentials or with an expired/invalid JWT token.
 *
 * Note: Spring Security already handles most 401 cases via the
 * JwtAuthenticationFilter. This exception is useful for
 * programmatic checks within service methods, e.g.:
 *   • Trying to approve your own leave request (if business rule)
 *   • Accessing another employee's payslip without ADMIN role
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
