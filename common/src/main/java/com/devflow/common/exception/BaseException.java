package com.devflow.common.exception;

import com.devflow.common.enums.ErrorCode;
import lombok.Getter;

//Extends RuntimeException = unchecked exception
// (doesnt need to be declared in method signatures)
//@Getter = Lombok generates getErrorCode() method

public class BaseException extends RuntimeException {

    //Which error code triggered this exception?
    private final ErrorCode errorCode;

    //Constructor 1: just an ErrorCode
    //Uses the ErrorCode's built in message automatically
    public BaseException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    //Constructor 2: ErrorCode + custom message override
    //Use when you want more details eg "User with id 42 not found"
    public BaseException(ErrorCode errorCode, String customMessage){
        super(customMessage)
        this.errorCode = errorCode;
    }

    //Constructor 3: ErrorCode + original cause
    //Use when wrapping another exception
    public  BaseException(ErrorCode errorCode, Throwable cause){
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

}
