package com.enterprise.hrm.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ============================================================
 * UNIFIED API RESPONSE WRAPPER
 * ============================================================
 *
 * WHY use a generic response wrapper?
 *   In enterprise APIs, every response should have a consistent
 *   envelope structure so clients always know what to expect:
 *
 *   {
 *     "success": true,
 *     "message": "Employee created successfully",
 *     "data": { ... },
 *     "timestamp": "2024-01-15T10:30:00"
 *   }
 *
 *   This allows:
 *   - Uniform error vs success responses
 *   - Clients can check 'success' flag without relying on HTTP status alone
 *   - Timestamp for debugging and distributed tracing
 *
 * @JsonInclude(JsonInclude.Include.NON_NULL)
 *   Jackson will not serialize fields that are null.
 *   E.g., on error responses, 'data' won't appear in JSON.
 *   This keeps the response payload clean.
 *
 * Generic <T>:
 *   The data payload type is parameterized so this wrapper works
 *   for ANY response: ApiResponse<EmployeeResponse>, ApiResponse<List<...>>
 *   etc. — full type safety.
 *
 * @Builder (Lombok):
 *   Generates a builder pattern. Usage:
 *     ApiResponse.<EmployeeResponse>builder()
 *         .success(true)
 *         .message("Created")
 *         .data(employeeResponse)
 *         .build();
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates whether the request succeeded.
     * true  → 2xx responses
     * false → 4xx / 5xx responses
     */
    private boolean success;

    /**
     * Human-readable message describing the result.
     * E.g.: "Employee created", "Validation failed", "Unauthorized"
     */
    private String message;

    /**
     * The actual response payload.
     * null for error responses, empty for 204 No Content.
     */
    private T data;

    /**
     * HTTP status code repeated in the body for client convenience.
     * (Some clients lose the actual HTTP status in proxy chains)
     */
    private int status;

    /**
     * Server-side timestamp of when this response was generated.
     * Used for debugging, distributed tracing, and cache validation.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ─────────────────────────────────────────────────────────────
    // FACTORY METHODS — convenient static constructors
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a successful response with data payload.
     * Used by: service calls that return data (GET, POST 201)
     */
    public static <T> ApiResponse<T> success(String message, T data, int status) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(status)
                .build();
    }

    /**
     * Creates a successful response without a data payload.
     * Used by: DELETE operations (204 No Content)
     */
    public static <T> ApiResponse<T> success(String message, int status) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .status(status)
                .build();
    }

    /**
     * Creates an error response.
     * Used by: GlobalExceptionHandler for all exception types.
     */
    public static <T> ApiResponse<T> error(String message, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .build();
    }
}
