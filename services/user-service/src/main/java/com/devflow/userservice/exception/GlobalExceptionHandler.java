package com.devflow.userservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Thrown by Spring when the DATABASE rejects data (e.g. a duplicate on our UNIQUE email column).
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
// ProblemDetail: Spring's class for the standard error JSON format (RFC 9457).
import org.springframework.http.ProblemDetail;
// Holds one failed validation rule: which field, and the message.
import org.springframework.validation.FieldError;
// Thrown by Spring when a @Valid request body breaks its rules.
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// @RestControllerAdvice: "this class handles exceptions thrown from ANY controller".
// One central place for error responses, instead of try/catch in every controller method.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler(X.class): "when an exception of type X escapes a controller, run this method".
    // Returning a ProblemDetail makes Spring send it as JSON, with the status code it contains
    // and the Content-Type "application/problem+json".

    // ---------- 400: validation failed ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // LinkedHashMap keeps insertion order, so errors appear in a predictable order.
        // The <String, String> generic: field name → error message.
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // putIfAbsent: keep only the FIRST error per field (a field can break several rules at once).
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        // Build the standard error body: status 400 plus a human-readable "detail".
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        problem.setTitle("Validation failed");    // a short summary of the problem type
        problem.setProperty("errors", errors);    // our extra field: per-field messages for the frontend
        return problem;
    }

    // ---------- 409: email already registered (caught by our own check in the service) ----------
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailExists(EmailAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Email already registered");
        return problem;
    }

    // ---------- 409: the database itself rejected duplicate data ----------
    // Why do we need this when the service already checks? A "race condition": if two requests
    // with the same email arrive at the SAME moment, both can pass the existsByEmail() check
    // before either one saves. The UNIQUE constraint in Postgres is the final safety net,
    // and this handler turns its error into a clean 409 instead of a 500 crash.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        // Log the technical details for us; don't send database internals to the client.
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "The request conflicts with existing data");
        problem.setTitle("Conflict");
        return problem;
    }

    // ---------- 404: user not found ----------
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("User not found");
        return problem;
    }
}
