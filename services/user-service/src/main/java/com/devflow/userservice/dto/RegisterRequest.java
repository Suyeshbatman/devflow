package com.devflow.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must be at most 255 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 to 72 characters")
    String password,

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    String fullName
){
    // [CONSTRUCTOR] A COMPACT CONSTRUCTOR: no parameter list and no field assignments.
    //   Java runs this code first and assigns the fields automatically afterwards,
    //   using the (possibly changed) parameter values.
    //   Order: Jackson calls this → fields are set → THEN the validator checks the annotations.
    public RegisterRequest {
        // [KEYWORD] if: a missing email can be null. Calling .trim() on null would crash
        //   (NullPointerException), so we only normalize when a value exists.
        //   @NotBlank still reports the "Email is required" error for null.
        if (email != null) {
            // [PARAMETER REASSIGNMENT] We change the PARAMETER; Java copies it into the field afterwards.
            //   [METHOD CALL] .trim() then .toLowerCase(...), both String methods returning NEW Strings.
            email = email.trim().toLowerCase(Locale.ROOT);
        }
        if (fullName != null) {
            // [METHOD CALL] Trim the name too, so "   " becomes "" and @NotBlank rejects it.
            fullName = fullName.trim();
        }
    }

}
