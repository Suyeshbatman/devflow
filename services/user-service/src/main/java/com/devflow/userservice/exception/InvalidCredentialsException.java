package com.devflow.userservice.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        // SECURITY: the SAME message whether the email or the password was wrong.
        // Saying "no such email" would let attackers discover which emails have accounts
        // (called "user enumeration").
        super("Invalid email or password");
    }

}
