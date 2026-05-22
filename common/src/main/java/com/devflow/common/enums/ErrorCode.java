package com.devflow.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    UNAUTHORIZED(401, "Authentication required"),
    FORBIDDEN(403, "You do not have permission to perform this action"),
    INVALID_TOKEN(401, "Token is invalid or expired"),
    TOKEN_EXPIRED(401, "Token has expired"),

    USER_NOT_FOUND(404, "User not found"),
    USER_ALREADY_EXISTS(409, "A user with this email already exists"),
    INVALID_CREDENTIALS(401, "Invalid email or password"),

    PRODUCT_NOT_FOUND(404, "Product not found"),
    ORDER_NOT_FOUND(404, "Order not found"),
    INSUFFICIENT_STOCK(400, "Insufficient stock available"),

    COURSE_NOT_FOUND(404, "Course not found"),
    ALREADY_ENROLLED(409, "You are already enrolled in this course"),

    VALIDATION_ERROR(400, "Validation failed"),
    INTERNAL_SERVER_ERROR(500, "An unexpected error occurred"),
    SERVICE_UNAVAILABLE(503, "Service is temporarily unavailable"),
    RESOURCE_NOT_FOUND(404, "Requested resource not found");

    private final int httpStatus;
    private final String message;
}