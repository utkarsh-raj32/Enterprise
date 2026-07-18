package com.enterprise.hrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ============================================================
 * DUPLICATE RESOURCE EXCEPTION (409 Conflict)
 * ============================================================
 *
 * Thrown when attempting to create a resource that already exists.
 * Examples:
 *   • Registering with an already-used email address
 *   • Creating a department with a duplicate code
 *   • Creating an employee with a duplicate employee code
 *
 * HTTP 409 Conflict is the correct status for uniqueness violations —
 * the request itself is valid, but conflicts with current server state.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
