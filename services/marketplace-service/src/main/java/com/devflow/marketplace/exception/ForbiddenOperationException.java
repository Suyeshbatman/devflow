package com.devflow.marketplace.exception;

// [CLASS] Thrown when a logged-in user tries to change something they don't own → 403.
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message){
        super(message);
    }
}
