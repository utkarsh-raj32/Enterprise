package com.enterprise.hrm.exception;

import com.enterprise.hrm.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * GLOBAL EXCEPTION HANDLER
 * ============================================================
 *
 * @RestControllerAdvice
 *   = @ControllerAdvice + @ResponseBody
 *
 *   @ControllerAdvice marks this class as a cross-cutting
 *   concern that intercepts exceptions thrown anywhere in the
 *   @Controller / @RestController layer BEFORE they propagate
 *   to the client.
 *
 *   This is the AOP (Aspect-Oriented Programming) approach to
 *   exception handling — we separate exception handling logic
 *   from business logic. Without this, every controller method
 *   would need try-catch blocks.
 *
 *   DESIGN PATTERN: This implements the "Chain of Responsibility"
 *   pattern — Spring walks up the exception hierarchy to find
 *   the most specific @ExceptionHandler that matches.
 *
 * @ExceptionHandler(SomeException.class)
 *   Maps a specific exception type to a handler method.
 *   Spring Security exceptions (AccessDeniedException) are
 *   handled here too — they bubble up through the filter chain.
 *
 * @Slf4j (Lombok)
 *   Generates: private static final Logger log = LoggerFactory.getLogger(...)
 *   SLF4J is a facade over Logback (default in Spring Boot).
 *   Always log exceptions with full stack trace in ERROR level.
 *
 * INTERVIEW: How does GlobalExceptionHandler work internally?
 *   Spring uses HandlerExceptionResolverComposite, which delegates
 *   to ExceptionHandlerExceptionResolver. When an exception is thrown,
 *   Spring scans @ControllerAdvice beans for the best-matching
 *   @ExceptionHandler method and invokes it.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────
    // 1. BEAN VALIDATION ERRORS — 400 Bad Request
    // ─────────────────────────────────────────────────────────────

    /**
     * Handles @Valid validation failures on @RequestBody or @RequestParam.
     * Spring throws MethodArgumentNotValidException when validation fails.
     *
     * We collect ALL field errors into a map:
     * { "email": "must be a valid email", "firstName": "must not be blank" }
     *
     * This gives clients enough information to fix all errors at once,
     * not just the first one encountered.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.error("Validation error on request [{}]: {}", request.getDescription(false), ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
          .getAllErrors()
          .forEach(error -> {
              String fieldName    = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
          });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ─────────────────────────────────────────────────────────────
    // 2. RESOURCE NOT FOUND — 404
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        log.error("Resource not found [{}]: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // 3. BUSINESS RULE VIOLATION — 400
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, WebRequest request) {

        log.error("Business rule violation [{}]: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // 4. DUPLICATE RESOURCE — 409 Conflict
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {

        log.error("Duplicate resource [{}]: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // 5. UNAUTHORIZED — 401
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex, WebRequest request) {

        log.error("Unauthorized access [{}]: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // 6. BAD CREDENTIALS (Spring Security) — 401
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        log.error("Bad credentials [{}]: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password", HttpStatus.UNAUTHORIZED.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // 7. ACCESS DENIED (Spring Security RBAC) — 403
    // ─────────────────────────────────────────────────────────────

    /**
     * Thrown when an authenticated user tries to access a resource
     * they don't have permission for (wrong role).
     * E.g., EMPLOYEE trying to access /api/v1/employees (ADMIN/HR only)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        log.error("Access denied [{}]: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You don't have permission to perform this action",
                        HttpStatus.FORBIDDEN.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // 8. ILLEGAL ARGUMENT — 400
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        log.error("Illegal argument [{}]: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    // ─────────────────────────────────────────────────────────────
    // 9. CATCH-ALL — 500 Internal Server Error
    // ─────────────────────────────────────────────────────────────

    /**
     * Safety net for any unhandled exception.
     * IMPORTANT: Never expose internal exception messages to clients in production!
     * Log the full stack trace internally but return a generic message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(
            Exception ex, WebRequest request) {

        // Log with full stack trace — critical for debugging
        log.error("Unexpected error occurred [{}]", request.getDescription(false), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please contact support.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }
}
