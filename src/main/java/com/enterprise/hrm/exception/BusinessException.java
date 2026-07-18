package com.enterprise.hrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ============================================================
 * BUSINESS LOGIC EXCEPTION (400)
 * ============================================================
 *
 * Used when a request is syntactically valid but violates
 * business rules. Examples:
 *   • Employee already has an approved leave for these dates
 *   • Cannot delete department that has active employees
 *   • Leave balance insufficient for requested days
 *   • Check-out attempted before check-in
 *
 * Difference from validation errors:
 *   @Valid catches field-level constraint violations (missing fields,
 *   format errors). BusinessException catches domain-level rule violations
 *   that require business logic to detect.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
