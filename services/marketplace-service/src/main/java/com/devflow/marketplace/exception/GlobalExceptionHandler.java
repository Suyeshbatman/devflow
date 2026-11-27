package com.devflow.marketplace.exception;

import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // [ANNOTATION] + [METHOD] 400: @Valid failed.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // [LOCAL VARIABLE] + [CONSTRUCTOR CALL] field name → first error message
        Map<String, String> errors = new LinkedHashMap<>();
        // for-each loop over every failed field
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // [METHOD CALL] putIfAbsent: keep only the first message per field.
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        // [STATIC METHOD CALL] Build the standard error body.
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    // [ANNOTATION] + [METHOD] 400: ?sort=banana (no such field on Product).
    //   Without this handler, the client would get a 500, as if the SERVER were broken. It's the client's mistake.
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleBadSort(PropertyReferenceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Cannot sort by '" + ex.getPropertyName() + "'");
        problem.setTitle("Invalid sort parameter");
        return problem;
    }

    // [ANNOTATION] + [METHOD] 404
    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleNotFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Product not found");
        return problem;
    }

    // [ANNOTATION] + [METHOD] 403: authenticated, but not allowed to touch THIS resource.
    @ExceptionHandler(ForbiddenOperationException.class)
    public ProblemDetail handleForbidden(ForbiddenOperationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Forbidden");
        return problem;
    }
}
