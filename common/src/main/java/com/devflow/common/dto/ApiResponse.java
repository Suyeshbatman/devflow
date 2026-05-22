package com.devflow.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// @Data = generates getters, setters, toString, equals, hashCode (Lombok)
// @Builder = lets us use ApiResponse.builder().success(true).build() pattern
// @NoArgsConstructor = generates empty constructor ApiResponse()
// @AllArgsConstructor = generates constructor with all fields
// @JsonInclude = don't include null fields in JSON output (cleaner responses)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ApiResponse<T>{

    //Was the request successful?
    private boolean success;

    //Human readable message ("User created", "Invalid token", etc.)
    private String message;

    //The actual payload could be a user, a List<Product>, anything
    //<T> means this is a Generic class - T is a placeholder for any type
    private T data;

    //When did this response happen?
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    //Static factory methods
    //These are convenience shortcuts so any service can do:
    // return ApiResponse.Success("User created", userDto);
    // return ApiResponse.error ("Email already exists");
    //instead of building the object manually every time

    public static <T> ApiResponse<T> success(String message, T data){
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message){
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error (String message){
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}