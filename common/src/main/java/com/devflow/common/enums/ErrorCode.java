package com.devflow.common.enums;

import lombock.Getter;
import lombock.RequiredArgsConstructor;

//@Getter = generates getters for httpStatus and message (Lombok)
//@RequiredArgsConstructor = generates constructor for final fields (Lombok)

@Getter
@RequiredArgsConstructor

public enum ErrorCode {
    // --Authentication & Authorization ---
    UNAUTHORIZED(401, "Authentication required"),
    FORBIDDEN(403, "You do not have permission to perform this action"),
    INVALID_TOKEN(401, "Token is invalid or expired"),
    TOKEN_EXPIRED(401, "Token has expired"),

    // --User Errors ---
    USER_NOT_FOUND(404, "User not found"),
    USER_ALREADY_EXISTS(409, "A user already exists with this email"),
    INVALID_CREDENTIALS(401, "Invalid email or password"),

    // -- Marketplace Errors ----
    PRODUCT_NOT_FOUND(404, "Product not found"),
    ORDER_NOT_FOUND(404, "Order not found"),
    INSUFFICIENT_STOCK(400, "Insuffecient stock available"),

    // --Learning Errors ---
    COURSE_NOT_FOUND(404, "Course not found"),
    ALREADY_ENROLLED(409, "You are already enrolled in this course"),

    // -- General Errors ---
    VALIDATION_ERROR(400, "Validation failed - check your input"),
    INTERNAL_SERVER_ERROR(500, "An unexpected error occured"),
    SERVICE_UNAVAILABLE_ERROR(503, "Service is temporarily unavailable"),
    RESOURCE_NOT_FOUND(404, "Requested resource not found"),

    //HTTP status code (401, 404, 500, etc)
    private final int httpStatus;

    //Developer/User friendly message
    private final String message;
}