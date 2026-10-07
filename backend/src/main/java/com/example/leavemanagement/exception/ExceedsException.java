package com.example.leavemanagement.exception;

import org.springframework.http.HttpStatus;

public class ExceedsException extends CustomException {
    
    public ExceedsException() {
        super("Not enough vacation balance");
        status = HttpStatus.BAD_REQUEST;
    }
}
