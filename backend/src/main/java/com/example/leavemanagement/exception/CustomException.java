package com.example.leavemanagement.exception;

import org.springframework.http.HttpStatus;

public abstract class CustomException extends Exception {

    protected HttpStatus status;

    public HttpStatus getStatus() {
        return status;
    }

    public CustomException(String message) {
        super(message);
    }
    
}
