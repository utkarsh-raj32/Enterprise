package com.enterprise.hrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ============================================================
 * RESOURCE NOT FOUND EXCEPTION (404)
 * ============================================================
 *
 * WHY custom exceptions instead of throwing RuntimeException?
 *   1. Semantic clarity — the exception name tells exactly what went wrong
 *   2. GlobalExceptionHandler can catch specific types and map them
 *      to precise HTTP status codes
 *   3. Clean service layer — no HTTP concerns in business logic
 *   4. Interview-friendly — shows knowledge of clean exception design
 *
 * @ResponseStatus(HttpStatus.NOT_FOUND):
 *   If this exception reaches Spring MVC without being caught by
 *   @ControllerAdvice, Spring automatically returns 404.
 *   This is a safety net — in practice our GlobalExceptionHandler catches it.
 *
 * Extends RuntimeException (unchecked):
 *   WHY unchecked? Forces callers to handle only exceptions they care about.
 *   We don't want every service method to declare `throws ResourceNotFoundException`
 *   — that pollutes method signatures and violates clean code principles.
 *   Spring's transaction rollback also works better with unchecked exceptions
 *   (@Transactional by default rolls back on RuntimeException only).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * @param resourceName The entity type (e.g., "Employee", "Department")
     * @param fieldName    The field used to look up (e.g., "id", "email")
     * @param fieldValue   The value that wasn't found (e.g., 42L, "john@email.com")
     *
     * Example message: "Employee not found with id : '42'"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName    = fieldName;
        this.fieldValue   = fieldValue;
    }

    public String getResourceName() { return resourceName; }
    public String getFieldName()    { return fieldName; }
    public Object getFieldValue()   { return fieldValue; }
}
